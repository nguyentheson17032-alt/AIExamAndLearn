package com.aiexam.warehouse.ai;

public record GradedAnswer(
        boolean correct,
        double scoreRatio,
        String feedback
) {
    public static GradedAnswer fallback(boolean correct, String feedback) {
        return new GradedAnswer(correct, correct ? 1.0 : 0.0, feedback);
    }
}
