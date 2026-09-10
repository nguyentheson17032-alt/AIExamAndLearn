package com.aiexam.warehouse.elo;

import org.springframework.stereotype.Component;

@Component
public class EloService {

    public int expectedScore(int userElo, int questionElo) {
        double exponent = (questionElo - userElo) / 400.0;
        return (int) Math.round((1.0 / (1.0 + Math.pow(10, exponent))) * 10000);
    }

    public double expectedScoreDecimal(int userElo, int questionElo) {
        return 1.0 / (1.0 + Math.pow(10, (questionElo - userElo) / 400.0));
    }

    public int nextRating(int currentElo, int opponentElo, double actualScore, int kFactor) {
        double expected = expectedScoreDecimal(currentElo, opponentElo);
        int updated = (int) Math.round(currentElo + kFactor * (actualScore - expected));
        return Math.max(100, updated);
    }

    public EloResult applyAttempt(int userElo, int averageQuestionElo, double scoreRatio, int userK, int questionK) {
        int nextUserElo = nextRating(userElo, averageQuestionElo, scoreRatio, userK);
        int nextQuestionElo = nextRating(averageQuestionElo, userElo, 1.0 - scoreRatio, questionK);
        return new EloResult(nextUserElo, nextQuestionElo, nextUserElo - userElo);
    }

    public record EloResult(int userElo, int questionElo, int userDelta) {}
}
