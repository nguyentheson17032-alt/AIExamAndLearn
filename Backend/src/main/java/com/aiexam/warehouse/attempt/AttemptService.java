package com.aiexam.warehouse.attempt;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.GradedAnswer;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.common.exception.ResourceNotFoundException;
import com.aiexam.warehouse.elo.EloChangeReason;
import com.aiexam.warehouse.elo.EloHistory;
import com.aiexam.warehouse.elo.EloHistoryRepository;
import com.aiexam.warehouse.elo.EloProperties;
import com.aiexam.warehouse.elo.EloService;
import com.aiexam.warehouse.exam.Exam;
import com.aiexam.warehouse.exam.ExamQuestion;
import com.aiexam.warehouse.exam.ExamRepository;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionChoice;
import com.aiexam.warehouse.question.QuestionType;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttemptService {

    private final AttemptRepository attemptRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final EloHistoryRepository eloHistoryRepository;
    private final EloService eloService;
    private final EloProperties eloProperties;
    private final ExamAiClient examAiClient;

    public Attempt getById(UUID id, UserPrincipal principal) {
        Attempt attempt = attemptRepository.findWithDetailsById(id)
                .orElseThrow(() -> ResourceNotFoundException.attempt(id));
        if (!attempt.getUser().getId().equals(principal.getId())) {
            throw new BusinessRuleViolationException("ATTEMPT_FORBIDDEN", "You cannot view this attempt");
        }
        return attempt;
    }

    public Page<Attempt> listMine(UserPrincipal principal, Pageable pageable) {
        return attemptRepository.findByUser_Id(principal.getId(), pageable);
    }

    @Transactional
    public Attempt start(UserPrincipal principal, UUID examId) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        Exam exam = examRepository.findWithQuestionsById(examId)
                .orElseThrow(() -> ResourceNotFoundException.exam(examId));
        if (exam.getQuestions().isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_EXAM", "Exam has no questions");
        }
        if (attemptRepository.existsByUser_IdAndExam_IdAndStatus(user.getId(), exam.getId(), AttemptStatus.IN_PROGRESS)) {
            throw new BusinessRuleViolationException("ATTEMPT_IN_PROGRESS", "An in-progress attempt already exists");
        }
        return attemptRepository.save(Attempt.start(user, exam));
    }

    @Transactional
    public Attempt saveAnswers(UserPrincipal principal, UUID attemptId, List<AnswerRequest> answers) {
        Attempt attempt = getById(attemptId, principal);
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new BusinessRuleViolationException("ATTEMPT_NOT_IN_PROGRESS", "Attempt is not in progress");
        }
        for (AnswerRequest request : answers) {
            Question question = attempt.getAnswers().stream()
                    .map(AttemptAnswer::getQuestion)
                    .filter(item -> item.getId().equals(request.questionId()))
                    .findFirst()
                    .orElseThrow(() -> new BusinessRuleViolationException(
                            "QUESTION_NOT_IN_ATTEMPT", "Question is not part of this attempt"));
            String selected = request.selectedChoiceIds() == null
                    ? null
                    : request.selectedChoiceIds().stream().map(UUID::toString).collect(Collectors.joining(","));
            attempt.answerFor(question).recordResponse(request.answerText(), selected);
        }
        return attempt;
    }

    @Transactional
    public Attempt submit(UserPrincipal principal, UUID attemptId) {
        Attempt attempt = getById(attemptId, principal);
        attempt.submit();
        User user = attempt.getUser();
        BigDecimal total = BigDecimal.ZERO;
        int questionEloSum = 0;
        int gradedCount = 0;
        for (AttemptAnswer answer : attempt.getAnswers()) {
            Question question = answer.getQuestion();
            int points = pointsFor(attempt.getExam(), question);
            GradedAnswer graded = grade(question, answer, points);
            BigDecimal awarded = BigDecimal.valueOf(points)
                    .multiply(BigDecimal.valueOf(graded.scoreRatio()))
                    .setScale(2, RoundingMode.HALF_UP);
            answer.grade(graded.correct(), awarded, graded.feedback());
            total = total.add(awarded);
            questionEloSum += question.getEloRating();
            gradedCount++;
            question.updateElo(eloService.nextRating(
                    question.getEloRating(),
                    user.getEloRating(),
                    1.0 - graded.scoreRatio(),
                    eloProperties.questionKFactor()));
        }
        BigDecimal maxScore = BigDecimal.valueOf(attempt.getExam().maxScore());
        double scoreRatio = maxScore.signum() == 0
                ? 0
                : total.divide(maxScore, 4, RoundingMode.HALF_UP).doubleValue();
        int averageQuestionElo = gradedCount == 0 ? user.getEloRating() : questionEloSum / gradedCount;
        int kFactor = examAiClient.recommendKFactor(user.getEloRating(), scoreRatio, eloProperties.defaultKFactor());
        int nextElo = eloService.nextRating(user.getEloRating(), averageQuestionElo, scoreRatio, kFactor);
        int before = user.getEloRating();
        user.updateElo(nextElo);
        attempt.completeGrading(total, maxScore, nextElo);
        eloHistoryRepository.save(EloHistory.record(
                user, attempt, before, nextElo, EloChangeReason.ATTEMPT_GRADED));
        return attempt;
    }

    private GradedAnswer grade(Question question, AttemptAnswer answer, int points) {
        if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
            Set<UUID> expected = question.correctChoices().stream()
                    .map(QuestionChoice::getId)
                    .collect(Collectors.toSet());
            Set<UUID> actual = parseChoiceIds(answer.getSelectedChoiceIds());
            boolean correct = expected.equals(actual);
            return new GradedAnswer(correct, correct ? 1.0 : 0.0, correct ? "Correct" : "Incorrect");
        }
        return examAiClient.gradeAnswer(
                question.getStem(),
                question.getOfficialAnswer(),
                answer.getAnswerText(),
                points);
    }

    private Set<UUID> parseChoiceIds(String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private int pointsFor(Exam exam, Question question) {
        return exam.getQuestions().stream()
                .filter(item -> item.getQuestion().getId().equals(question.getId()))
                .mapToInt(ExamQuestion::getPoints)
                .findFirst()
                .orElse(1);
    }

    public record AnswerRequest(
            @NotNull UUID questionId,
            String answerText,
            List<UUID> selectedChoiceIds
    ) {}

    public record AttemptResponse(
            UUID id,
            UUID examId,
            AttemptStatus status,
            Instant startedAt,
            Instant submittedAt,
            BigDecimal score,
            BigDecimal maxScore,
            Integer eloBefore,
            Integer eloAfter,
            List<AttemptAnswerResponse> answers
    ) {
        public static AttemptResponse from(Attempt attempt) {
            return new AttemptResponse(
                    attempt.getId(),
                    attempt.getExam().getId(),
                    attempt.getStatus(),
                    attempt.getStartedAt(),
                    attempt.getSubmittedAt(),
                    attempt.getScore(),
                    attempt.getMaxScore(),
                    attempt.getEloBefore(),
                    attempt.getEloAfter(),
                    attempt.getAnswers().stream().map(AttemptAnswerResponse::from).toList());
        }
    }

    public record AttemptAnswerResponse(
            UUID questionId,
            String answerText,
            String selectedChoiceIds,
            Boolean correct,
            BigDecimal scoreAwarded,
            String aiFeedback
    ) {
        public static AttemptAnswerResponse from(AttemptAnswer answer) {
            return new AttemptAnswerResponse(
                    answer.getQuestion().getId(),
                    answer.getAnswerText(),
                    answer.getSelectedChoiceIds(),
                    answer.getCorrect(),
                    answer.getScoreAwarded(),
                    answer.getAiFeedback());
        }
    }
}
