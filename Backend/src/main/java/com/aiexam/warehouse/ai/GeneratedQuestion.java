package com.aiexam.warehouse.ai;

import com.aiexam.warehouse.question.Difficulty;
import com.aiexam.warehouse.question.QuestionType;
import java.util.List;

public record GeneratedQuestion(
        String stem,
        QuestionType questionType,
        String subject,
        String topic,
        Difficulty difficulty,
        String officialAnswer,
        String explanation,
        List<GeneratedChoice> choices
) {
    public record GeneratedChoice(String label, String content, boolean correct) {}
}
