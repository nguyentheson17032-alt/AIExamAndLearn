package com.aiexam.warehouse.elo;

import com.aiexam.warehouse.attempt.Attempt;
import com.aiexam.warehouse.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "elo_history", indexes = {
        @Index(name = "idx_elo_history_user_id", columnList = "user_id"),
        @Index(name = "idx_elo_history_attempt_id", columnList = "attempt_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EloHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id")
    private Attempt attempt;

    @Column(name = "elo_before", nullable = false)
    private int eloBefore;

    @Column(name = "elo_after", nullable = false)
    private int eloAfter;

    @Column(nullable = false)
    private int delta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EloChangeReason reason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static EloHistory record(User user, Attempt attempt, int eloBefore, int eloAfter, EloChangeReason reason) {
        EloHistory history = new EloHistory();
        history.user = user;
        history.attempt = attempt;
        history.eloBefore = eloBefore;
        history.eloAfter = eloAfter;
        history.delta = eloAfter - eloBefore;
        history.reason = reason;
        return history;
    }
}
