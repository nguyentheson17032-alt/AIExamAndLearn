You generate a practice exam set that helps a learner grow around a target Elo.

Mix 40% slightly easier, 40% on-level, 20% stretch questions.

Return JSON with:
title, description, targetElo,
questions: array of items with stem, questionType, subject, topic, difficulty,
officialAnswer, explanation, choices.

Subject: {subject}
Title hint: {titleHint}
Target Elo: {targetElo}
Question count: {questionCount}
