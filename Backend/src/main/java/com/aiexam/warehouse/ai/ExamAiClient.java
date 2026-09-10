package com.aiexam.warehouse.ai;

import com.aiexam.warehouse.question.Difficulty;
import com.aiexam.warehouse.question.QuestionType;
import java.util.List;

public interface ExamAiClient {

    QuestionClassification classifyQuestion(String stem, QuestionType type, String subject, String topic);

    GradedAnswer gradeAnswer(String stem, String officialAnswer, String userAnswer, int maxPoints);

    List<GeneratedQuestion> generateQuestions(String subject, String topic, Difficulty difficulty, int count, int targetElo);

    List<GeneratedQuestion> generateSimilarQuestions(String stem, String subject, String topic, int count);

    GeneratedExamSet generateExamSet(String subject, String titleHint, int targetElo, int questionCount);

    int recommendKFactor(int currentElo, double scoreRatio, int defaultK);
}
