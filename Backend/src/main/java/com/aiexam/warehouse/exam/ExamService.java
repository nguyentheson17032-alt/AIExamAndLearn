package com.aiexam.warehouse.exam;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.GeneratedExamSet;
import com.aiexam.warehouse.ai.GeneratedQuestion;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.common.exception.ResourceNotFoundException;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.question.QuestionRepository;
import com.aiexam.warehouse.question.QuestionService;
import com.aiexam.warehouse.question.QuestionSource;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExamService {

    private final ExamRepository examRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final QuestionService questionService;
    private final ExamAiClient examAiClient;

    public Exam getById(UUID id) {
        return examRepository.findWithQuestionsById(id)
                .orElseThrow(() -> ResourceNotFoundException.exam(id));
    }

    public Page<Exam> list(ExamType examType, Pageable pageable) {
        if (examType == null) {
            return examRepository.findByStatus(ExamStatus.PUBLISHED, pageable);
        }
        return examRepository.findByStatusAndExamType(ExamStatus.PUBLISHED, examType, pageable);
    }

    @Transactional
    public Exam create(UserPrincipal principal, CreateExamRequest request) {
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        Exam exam = Exam.create(
                author,
                request.title(),
                request.description(),
                request.examType(),
                ExamSource.USER_CREATED,
                request.targetElo(),
                request.timeLimitMinutes());
        for (UUID questionId : request.questionIds()) {
            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> ResourceNotFoundException.question(questionId));
            exam.addQuestion(question, 1);
        }
        return examRepository.save(exam);
    }

    @Transactional
    public Exam generateExamSet(UserPrincipal principal, GenerateExamRequest request) {
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        int targetElo = request.targetElo() != null ? request.targetElo() : author.getEloRating();
        GeneratedExamSet generated = examAiClient.generateExamSet(
                request.subject(),
                request.title(),
                targetElo,
                request.questionCount());
        if (generated.questions() == null || generated.questions().isEmpty()) {
            throw new BusinessRuleViolationException("EXAM_GENERATION_FAILED", "AI did not generate any questions");
        }
        Exam exam = Exam.create(
                author,
                generated.title(),
                generated.description(),
                request.examType(),
                ExamSource.AI_GENERATED,
                generated.targetElo(),
                request.timeLimitMinutes());
        for (GeneratedQuestion item : generated.questions()) {
            Question question = questionService.persistGenerated(author, item, QuestionSource.AI_GENERATED);
            exam.addQuestion(question, 1);
        }
        return examRepository.save(exam);
    }

    @Transactional
    public Exam generateSimilar(UserPrincipal principal, UUID examId, int count) {
        Exam source = getById(examId);
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        Exam similar = Exam.create(
                author,
                "Similar: " + source.getTitle(),
                "AI generated similar exercises",
                ExamType.EXERCISE,
                ExamSource.AI_GENERATED,
                source.getTargetElo(),
                source.getTimeLimitMinutes());
        List<ExamQuestion> sourceQuestions = source.getQuestions();
        if (sourceQuestions.isEmpty()) {
            throw new BusinessRuleViolationException("EMPTY_EXAM", "Source exam has no questions");
        }
        int perQuestion = Math.max(1, count / sourceQuestions.size());
        for (ExamQuestion item : sourceQuestions) {
            Question original = item.getQuestion();
            List<GeneratedQuestion> generated = examAiClient.generateSimilarQuestions(
                    original.getStem(), original.getSubject(), original.getTopic(), perQuestion);
            for (GeneratedQuestion generatedQuestion : generated) {
                Question saved = questionService.persistGenerated(author, generatedQuestion, QuestionSource.AI_GENERATED);
                similar.addQuestion(saved, item.getPoints());
            }
        }
        return examRepository.save(similar);
    }

    public record CreateExamRequest(
            @NotBlank String title,
            String description,
            @NotNull ExamType examType,
            Integer targetElo,
            Integer timeLimitMinutes,
            @NotEmpty List<UUID> questionIds
    ) {}

    public record GenerateExamRequest(
            @NotBlank String subject,
            String title,
            @NotNull ExamType examType,
            Integer targetElo,
            @jakarta.validation.constraints.Min(3) @jakarta.validation.constraints.Max(30) int questionCount,
            Integer timeLimitMinutes
    ) {}

    public record ExamResponse(
            UUID id,
            String title,
            String description,
            ExamType examType,
            ExamSource source,
            Integer targetElo,
            ExamStatus status,
            Integer timeLimitMinutes,
            int questionCount,
            List<ExamQuestionResponse> questions
    ) {
        public static ExamResponse from(Exam exam) {
            return new ExamResponse(
                    exam.getId(),
                    exam.getTitle(),
                    exam.getDescription(),
                    exam.getExamType(),
                    exam.getSource(),
                    exam.getTargetElo(),
                    exam.getStatus(),
                    exam.getTimeLimitMinutes(),
                    exam.getQuestions().size(),
                    exam.getQuestions().stream().map(ExamQuestionResponse::from).toList());
        }

        public static ExamResponse summary(Exam exam) {
            return new ExamResponse(
                    exam.getId(),
                    exam.getTitle(),
                    exam.getDescription(),
                    exam.getExamType(),
                    exam.getSource(),
                    exam.getTargetElo(),
                    exam.getStatus(),
                    exam.getTimeLimitMinutes(),
                    exam.getQuestions().size(),
                    List.of());
        }
    }

    public record ExamQuestionResponse(
            UUID questionId,
            int position,
            int points,
            QuestionService.QuestionResponse question
    ) {
        public static ExamQuestionResponse from(ExamQuestion item) {
            return new ExamQuestionResponse(
                    item.getQuestion().getId(),
                    item.getPosition(),
                    item.getPoints(),
                    QuestionService.QuestionResponse.from(item.getQuestion()));
        }
    }
}
