package com.aiexam.warehouse.question;

import com.aiexam.warehouse.ai.ExamAiClient;
import com.aiexam.warehouse.ai.GeneratedQuestion;
import com.aiexam.warehouse.ai.QuestionClassification;
import com.aiexam.warehouse.auth.UserPrincipal;
import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.common.exception.ResourceNotFoundException;
import com.aiexam.warehouse.user.User;
import com.aiexam.warehouse.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
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
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ExamAiClient examAiClient;

    public Question getById(UUID id) {
        return questionRepository.findWithChoicesById(id)
                .orElseThrow(() -> ResourceNotFoundException.question(id));
    }

    public Page<Question> list(String subject, Pageable pageable) {
        if (subject == null || subject.isBlank()) {
            return questionRepository.findByStatus(PublishStatus.PUBLISHED, pageable);
        }
        return questionRepository.findByStatusAndSubject(PublishStatus.PUBLISHED, subject, pageable);
    }

    @Transactional
    public Question upload(UserPrincipal principal, UploadQuestionRequest request) {
        validateChoices(request.questionType(), request.choices());
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        Question question = Question.create(
                author,
                request.stem(),
                request.questionType(),
                request.subject(),
                request.topic(),
                QuestionSource.USER_UPLOAD);
        question.setExplanation(request.explanation());
        question.setOfficialAnswer(request.officialAnswer());
        if (request.choices() != null) {
            for (int i = 0; i < request.choices().size(); i++) {
                ChoiceRequest choice = request.choices().get(i);
                question.addChoice(choice.label(), choice.content(), choice.correct(), i + 1);
            }
        }
        QuestionClassification classification = examAiClient.classifyQuestion(
                question.getStem(), question.getQuestionType(), question.getSubject(), question.getTopic());
        question.classify(
                classification.difficulty(),
                classification.bloomLevel(),
                classification.tags(),
                classification.suggestedElo());
        return questionRepository.save(question);
    }

    @Transactional
    public Question classify(UUID id) {
        Question question = getById(id);
        QuestionClassification classification = examAiClient.classifyQuestion(
                question.getStem(), question.getQuestionType(), question.getSubject(), question.getTopic());
        question.classify(
                classification.difficulty(),
                classification.bloomLevel(),
                classification.tags(),
                classification.suggestedElo());
        return question;
    }

    @Transactional
    public List<Question> generate(UserPrincipal principal, GenerateQuestionsRequest request) {
        User author = userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.user(principal.getUsername()));
        List<GeneratedQuestion> generated = examAiClient.generateQuestions(
                request.subject(),
                request.topic(),
                request.difficulty(),
                request.count(),
                request.targetElo());
        return generated.stream()
                .map(item -> persistGenerated(author, item, QuestionSource.AI_GENERATED))
                .toList();
    }

    public Question persistGenerated(User author, GeneratedQuestion item, QuestionSource source) {
        Question question = Question.create(
                author,
                item.stem(),
                item.questionType(),
                item.subject(),
                item.topic(),
                source);
        question.setExplanation(item.explanation());
        question.setOfficialAnswer(item.officialAnswer());
        question.classify(item.difficulty(), null, item.topic(), question.getEloRating());
        if (item.choices() != null) {
            for (int i = 0; i < item.choices().size(); i++) {
                var choice = item.choices().get(i);
                question.addChoice(choice.label(), choice.content(), choice.correct(), i + 1);
            }
        }
        return questionRepository.save(question);
    }

    private void validateChoices(QuestionType type, List<ChoiceRequest> choices) {
        if (type == QuestionType.MULTIPLE_CHOICE) {
            if (choices == null || choices.size() < 2) {
                throw new BusinessRuleViolationException("INVALID_CHOICES", "Multiple choice questions need at least 2 choices");
            }
            boolean hasCorrect = choices.stream().anyMatch(ChoiceRequest::correct);
            if (!hasCorrect) {
                throw new BusinessRuleViolationException("INVALID_CHOICES", "Multiple choice questions need a correct choice");
            }
        }
    }

    public record UploadQuestionRequest(
            @NotBlank String stem,
            @NotNull QuestionType questionType,
            @NotBlank String subject,
            @NotBlank String topic,
            String explanation,
            String officialAnswer,
            List<ChoiceRequest> choices
    ) {}

    public record ChoiceRequest(
            @NotBlank String label,
            @NotBlank String content,
            boolean correct
    ) {}

    public record GenerateQuestionsRequest(
            @NotBlank String subject,
            @NotBlank String topic,
            @NotNull Difficulty difficulty,
            @jakarta.validation.constraints.Min(1) @jakarta.validation.constraints.Max(10) int count,
            @jakarta.validation.constraints.Min(800) int targetElo
    ) {}

    public record QuestionResponse(
            UUID id,
            String stem,
            QuestionType questionType,
            String subject,
            String topic,
            Difficulty difficulty,
            int eloRating,
            String bloomLevel,
            String tags,
            String explanation,
            QuestionSource source,
            PublishStatus status,
            List<ChoiceResponse> choices
    ) {
        public static QuestionResponse from(Question question) {
            return new QuestionResponse(
                    question.getId(),
                    question.getStem(),
                    question.getQuestionType(),
                    question.getSubject(),
                    question.getTopic(),
                    question.getDifficulty(),
                    question.getEloRating(),
                    question.getBloomLevel(),
                    question.getTags(),
                    question.getExplanation(),
                    question.getSource(),
                    question.getStatus(),
                    question.getChoices().stream().map(ChoiceResponse::from).toList());
        }
    }

    public record ChoiceResponse(UUID id, String label, String content) {
        public static ChoiceResponse from(QuestionChoice choice) {
            return new ChoiceResponse(choice.getId(), choice.getLabel(), choice.getContent());
        }
    }

    public record QuestionAdminResponse(
            UUID id,
            String stem,
            QuestionType questionType,
            String subject,
            String topic,
            Difficulty difficulty,
            int eloRating,
            String officialAnswer,
            List<ChoiceAdminResponse> choices
    ) {
        public static QuestionAdminResponse from(Question question) {
            return new QuestionAdminResponse(
                    question.getId(),
                    question.getStem(),
                    question.getQuestionType(),
                    question.getSubject(),
                    question.getTopic(),
                    question.getDifficulty(),
                    question.getEloRating(),
                    question.getOfficialAnswer(),
                    question.getChoices().stream().map(ChoiceAdminResponse::from).toList());
        }
    }

    public record ChoiceAdminResponse(UUID id, String label, String content, boolean correct) {
        public static ChoiceAdminResponse from(QuestionChoice choice) {
            return new ChoiceAdminResponse(choice.getId(), choice.getLabel(), choice.getContent(), choice.isCorrect());
        }
    }
}
