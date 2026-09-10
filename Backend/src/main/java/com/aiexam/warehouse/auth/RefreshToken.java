package com.aiexam.warehouse.auth;

import com.aiexam.warehouse.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "family_id", nullable = false)
    private UUID familyId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public static RefreshToken issue(User user, String tokenHash, UUID familyId, Instant expiresAt) {
        RefreshToken token = new RefreshToken();
        token.user = user;
        token.tokenHash = tokenHash;
        token.familyId = familyId;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean isActive() {
        return revokedAt == null && expiresAt.isAfter(Instant.now());
    }

    public void revoke(UUID replacementId) {
        this.revokedAt = Instant.now();
        this.replacedBy = replacementId;
    }

    public void revokeFamily() {
        this.revokedAt = Instant.now();
    }
}
