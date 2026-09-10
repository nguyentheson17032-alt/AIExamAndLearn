package com.aiexam.warehouse.ai;

import com.aiexam.warehouse.question.Difficulty;
import com.aiexam.warehouse.question.QuestionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "true")
public class SpringAiExamAiClient implements ExamAiClient {

    private final ChatClient chatClient;

    @Value("classpath:prompts/classify-question.st")
    private Resource classifyPrompt;

    @Value("classpath:prompts/grade-answer.st")
    private Resource gradePrompt;

    @Value("classpath:prompts/generate-questions.st")
    private Resource generatePrompt;

    @Value("classpath:prompts/generate-similar-questions.st")
    private Resource similarPrompt;

    @Value("classpath:prompts/generate-exam-set.st")
    private Resource examSetPrompt;

    @Value("classpath:prompts/recommend-k-factor.st")
    private Resource kFactorPrompt;

    @Override
    @Retryable(retryFor = TransientAiException.class, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public QuestionClassification classifyQuestion(String stem, QuestionType type, String subject, String topic) {
        return chatClient.prompt()
                .user(u -> u.text(classifyPrompt)
                        .param("stem", stem)
                        .param("type", type.name())
                        .param("subject", subject)
                        .param("topic", topic))
                .call()
                .entity(QuestionClassification.class);
    }

    @Recover
    public QuestionClassification classifyQuestion(NonTransientAiException error, String stem, QuestionType type,
            String subject, String topic) {
        log.error("Question classification failed permanently", error);
        return QuestionClassification.fallback("AI classification unavailable");
    }

    @Recover
    public QuestionClassification classifyQuestion(TransientAiException error, String stem, QuestionType type,
            String subject, String topic) {
        log.error("Question classification retries exhausted", error);
        return QuestionClassification.fallback("AI classification unavailable");
    }

    @Override
    @Retryable(retryFor = TransientAiException.class, maxAttempts = 3, backoff = @Backoff(delay = 200, multiplier = 2))
    public GradedAnswer gradeAnswer(String stem, String officialAnswer, String userAnswer, int maxPoints) {
        return chatClient.prompt()
                .user(u -> u.text(gradePrompt)
                        .param("stem", stem)
                        .param("officialAnswer", officialAnswer == null ? "" : officialAnswer)
                        .param("userAnswer", userAnswer == null ? "" : userAnswer)
                        .param("maxPoints", maxPoints))
                .call()
                .entity(GradedAnswer.class);
    }

    @Recover
    public GradedAnswer gradeAnswer(Exception error, String stem, String officialAnswer, String userAnswer, int maxPoints) {
        log.error("AI grading failed", error);
        return GradedAnswer.fallback(false, "AI grading unavailable");
    }

    @Override
    public List<GeneratedQuestion> generateQuestions(
            String subject, String topic, Difficulty difficulty, int count, int targetElo) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(generatePrompt)
                            .param("subject", subject)
                            .param("topic", topic)
                            .param("difficulty", difficulty.name())
                            .param("count", count)
                            .param("targetElo", targetElo))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Question generation failed", e);
            return List.of();
        }
    }

    @Override
    public List<GeneratedQuestion> generateSimilarQuestions(String stem, String subject, String topic, int count) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(similarPrompt)
                            .param("stem", stem)
                            .param("subject", subject)
                            .param("topic", topic)
                            .param("count", count))
                    .call()
                    .entity(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.error("Similar question generation failed", e);
            return List.of();
        }
    }

    @Override
    public GeneratedExamSet generateExamSet(String subject, String titleHint, int targetElo, int questionCount) {
        try {
            return chatClient.prompt()
                    .user(u -> u.text(examSetPrompt)
                            .param("subject", subject)
                            .param("titleHint", titleHint == null ? "" : titleHint)
                            .param("targetElo", targetElo)
                            .param("questionCount", questionCount))
                    .call()
                    .entity(GeneratedExamSet.class);
        } catch (Exception e) {
            log.error("Exam set generation failed", e);
            return new GeneratedExamSet(subject + " practice", "Fallback empty set", targetElo, List.of());
        }
    }

    @Override
    public int recommendKFactor(int currentElo, double scoreRatio, int defaultK) {
        try {
            KFactorRecommendation recommendation = chatClient.prompt()
                    .user(u -> u.text(kFactorPrompt)
                            .param("currentElo", currentElo)
                            .param("scoreRatio", scoreRatio)
                            .param("defaultK", defaultK))
                    .call()
                    .entity(KFactorRecommendation.class);
            if (recommendation == null || recommendation.kFactor() < 4 || recommendation.kFactor() > 64) {
                return defaultK;
            }
            return recommendation.kFactor();
        } catch (Exception e) {
            log.error("K-factor recommendation failed", e);
            return defaultK;
        }
    }
}
