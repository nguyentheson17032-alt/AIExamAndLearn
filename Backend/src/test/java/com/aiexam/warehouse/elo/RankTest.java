package com.aiexam.warehouse.elo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RankTest {

    @Test
    void fromElo_shouldMapBronzeThroughDiamond() {
        assertThat(Rank.fromElo(800)).isEqualTo(Rank.BRONZE);
        assertThat(Rank.fromElo(1000)).isEqualTo(Rank.SILVER);
        assertThat(Rank.fromElo(1400)).isEqualTo(Rank.GOLD);
        assertThat(Rank.fromElo(1700)).isEqualTo(Rank.PLATINUM);
        assertThat(Rank.fromElo(2000)).isEqualTo(Rank.DIAMOND);
    }
}
