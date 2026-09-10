package com.aiexam.warehouse.attempt;

import com.aiexam.warehouse.common.exception.BusinessRuleViolationException;
import com.aiexam.warehouse.exam.Exam;
import com.aiexam.warehouse.question.Question;
import com.aiexam.warehouse.user.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "attempts", indexes = {
        @Index(name = "idx_attempts_user_id", columnList = "user_id"),
        @Index(name = "idx_attempts_exam_id", columnList = "exam_id"),
        @Index(name = "idx_attempts_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AttemptStatus status;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(precision = 8, scale = 2)
    private BigDecimal score;

    @Column(name = "max_score", precision = 8, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "elo_before")
    private Integer eloBefore;

    @Column(name = "elo_after")
    private Integer eloAfter;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttemptAnswer> answers = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public static Attempt start(User user, Exam exam) {
        Attempt attempt = new Attempt();
        attempt.user = user;
        attempt.exam = exam;
        attempt.status = AttemptStatus.IN_PROGRESS;
        attempt.startedAt = Instant.now();
        attempt.eloBefore = user.getEloRating();
        exam.getQuestions().forEach(item ->
                attempt.answers.add(AttemptAnswer.create(attempt, item.getQuestion())));
        return attempt;
    }

    public AttemptAnswer answerFor(Question question) {
        return answers.stream()
                .filter(answer -> answer.getQuestion().getId().equals(question.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "QUESTION_NOT_IN_ATTEMPT", "Question is not part of this attempt"));
    }

    public void submit() {
        if (status != AttemptStatus.IN_PROGRESS) {
            throw new BusinessRuleViolationException("ATTEMPT_NOT_IN_PROGRESS", "Attempt is not in progress");
        }
        status = AttemptStatus.SUBMITTED;
        submittedAt = Instant.now();
    }

    public void completeGrading(BigDecimal score, BigDecimal maxScore, int eloAfter) {
        this.score = score;
        this.maxScore = maxScore;
        this.eloAfter = eloAfter;
        this.status = AttemptStatus.GRADED;
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }
}
