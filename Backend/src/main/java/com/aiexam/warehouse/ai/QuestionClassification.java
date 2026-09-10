package com.aiexam.warehouse.ai;

import com.aiexam.warehouse.question.Difficulty;

public record QuestionClassification(
        Difficulty difficulty,
        String bloomLevel,
        String tags,
        int suggestedElo,
        String rationale
) {
    public static QuestionClassification fallback(String reason) {
        return new QuestionClassification(Difficulty.INTERMEDIATE, "UNDERSTAND", "uncategorized", 1200, reason);
    }
}
