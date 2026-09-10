package com.aiexam.warehouse.ai;

import java.util.List;

public record GeneratedExamSet(
        String title,
        String description,
        int targetElo,
        List<GeneratedQuestion> questions
) {}
