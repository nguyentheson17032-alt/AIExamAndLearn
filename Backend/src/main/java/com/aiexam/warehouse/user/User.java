package com.aiexam.warehouse.user;

import com.aiexam.warehouse.elo.Rank;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_elo_rating", columnList = "elo_rating")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    public static final int DEFAULT_ELO = 1200;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(name = "elo_rating", nullable = false)
    private int eloRating;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean locked;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public static User register(String email, String passwordHash, String displayName) {
        User user = new User();
        user.email = email.toLowerCase();
        user.passwordHash = passwordHash;
        user.displayName = displayName;
        user.role = Role.USER;
        user.eloRating = DEFAULT_ELO;
        user.enabled = true;
        user.locked = false;
        return user;
    }

    public Rank rank() {
        return Rank.fromElo(eloRating);
    }

    public void updateElo(int newRating) {
        this.eloRating = newRating;
    }

    public boolean isAccountUsable() {
        return enabled && !locked;
    }
}
