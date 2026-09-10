package com.aiexam.warehouse.elo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EloServiceTest {

    private final EloService eloService = new EloService();

    @Test
    void nextRating_whenUserScoresHigherThanExpected_shouldIncreaseElo() {
        int updated = eloService.nextRating(1200, 1200, 1.0, 24);

        assertThat(updated).isGreaterThan(1200);
    }

    @Test
    void nextRating_whenUserScoresLowerThanExpected_shouldDecreaseElo() {
        int updated = eloService.nextRating(1200, 1200, 0.0, 24);

        assertThat(updated).isLessThan(1200);
    }

    @Test
    void applyAttempt_shouldMoveUserAndQuestionInOppositeDirections() {
        EloService.EloResult result = eloService.applyAttempt(1400, 1200, 0.0, 24, 12);

        assertThat(result.userElo()).isLessThan(1400);
        assertThat(result.questionElo()).isGreaterThan(1200);
        assertThat(result.userDelta()).isNegative();
    }

    @Test
    void nextRating_shouldNotFallBelow100() {
        int updated = eloService.nextRating(100, 2000, 0.0, 32);

        assertThat(updated).isEqualTo(100);
    }
}
