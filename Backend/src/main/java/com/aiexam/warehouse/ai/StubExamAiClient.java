package com.aiexam.warehouse.ai;

import com.aiexam.warehouse.question.Difficulty;
import com.aiexam.warehouse.question.QuestionType;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.ai.enabled", havingValue = "false", matchIfMissing = true)
public class StubExamAiClient implements ExamAiClient {

    @Override
    public QuestionClassification classifyQuestion(String stem, QuestionType type, String subject, String topic) {
        Difficulty difficulty = stem.length() > 280 ? Difficulty.ADVANCED : Difficulty.INTERMEDIATE;
        int elo = switch (difficulty) {
            case BEGINNER -> 900;
            case INTERMEDIATE -> 1200;
            case ADVANCED -> 1500;
            case EXPERT -> 1800;
        };
        return new QuestionClassification(difficulty, "APPLY", subject + "," + topic, elo, "Heuristic classification");
    }

    @Override
    public GradedAnswer gradeAnswer(String stem, String officialAnswer, String userAnswer, int maxPoints) {
        if (userAnswer == null || userAnswer.isBlank()) {
            return GradedAnswer.fallback(false, "Empty answer");
        }
        boolean correct = officialAnswer != null
                && userAnswer.trim().equalsIgnoreCase(officialAnswer.trim());
        return new GradedAnswer(
                correct,
                correct ? 1.0 : 0.0,
                correct ? "Matches the official answer" : "Does not match the official answer");
    }

    @Override
    public List<GeneratedQuestion> generateQuestions(
            String subject, String topic, Difficulty difficulty, int count, int targetElo) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> sampleQuestion(subject, topic, difficulty, i))
                .toList();
    }

    @Override
    public List<GeneratedQuestion> generateSimilarQuestions(String stem, String subject, String topic, int count) {
        return java.util.stream.IntStream.rangeClosed(1, count)
                .mapToObj(i -> sampleQuestion(subject, topic, Difficulty.INTERMEDIATE, i))
                .toList();
    }

    @Override
    public GeneratedExamSet generateExamSet(String subject, String titleHint, int targetElo, int questionCount) {
        Difficulty difficulty = RankLike.fromElo(targetElo);
        List<GeneratedQuestion> questions = generateQuestions(subject, subject, difficulty, questionCount, targetElo);
        String title = titleHint == null || titleHint.isBlank()
                ? subject + " practice set"
                : titleHint;
        return new GeneratedExamSet(title, "Adaptive practice around Elo " + targetElo, targetElo, questions);
    }

    @Override
    public int recommendKFactor(int currentElo, double scoreRatio, int defaultK) {
        if (currentElo > 1800) {
            return Math.max(8, defaultK - 8);
        }
        if (scoreRatio >= 0.9 || scoreRatio <= 0.1) {
            return defaultK + 4;
        }
        return defaultK;
    }

    private GeneratedQuestion sampleQuestion(String subject, String topic, Difficulty difficulty, int index) {
        String stem = "Practice " + subject + " / " + topic + " #" + index
                + " [" + difficulty.name().toLowerCase(Locale.ROOT) + "]: choose the correct statement.";
        return new GeneratedQuestion(
                stem,
                QuestionType.MULTIPLE_CHOICE,
                subject,
                topic,
                difficulty,
                "A",
                "Stub generated question for local/dev use",
                List.of(
                        new GeneratedQuestion.GeneratedChoice("A", "Correct statement", true),
                        new GeneratedQuestion.GeneratedChoice("B", "Plausible distractor", false),
                        new GeneratedQuestion.GeneratedChoice("C", "Common misconception", false),
                        new GeneratedQuestion.GeneratedChoice("D", "Irrelevant option", false)));
    }

    private enum RankLike {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT;

        static Difficulty fromElo(int elo) {
            if (elo < 1000) {
                return Difficulty.BEGINNER;
            }
            if (elo < 1400) {
                return Difficulty.INTERMEDIATE;
            }
            if (elo < 1700) {
                return Difficulty.ADVANCED;
            }
            return Difficulty.EXPERT;
        }
    }
}
