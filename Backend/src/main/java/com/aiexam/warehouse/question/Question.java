package com.aiexam.warehouse.question;

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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
@Table(name = "questions", indexes = {
        @Index(name = "idx_questions_created_by_id", columnList = "created_by_id"),
        @Index(name = "idx_questions_subject", columnList = "subject"),
        @Index(name = "idx_questions_topic", columnList = "topic"),
        @Index(name = "idx_questions_difficulty", columnList = "difficulty"),
        @Index(name = "idx_questions_elo_rating", columnList = "elo_rating"),
        @Index(name = "idx_questions_status", columnList = "status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    public static final int DEFAULT_ELO = 1200;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String stem;

    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false, length = 50)
    private QuestionType questionType;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, length = 150)
    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Difficulty difficulty;

    @Column(name = "elo_rating", nullable = false)
    private int eloRating;

    @Column(name = "bloom_level", length = 50)
    private String bloomLevel;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "official_answer", columnDefinition = "TEXT")
    private String officialAnswer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuestionSource source;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PublishStatus status;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionChoice> choices = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public static Question create(
            User createdBy,
            String stem,
            QuestionType questionType,
            String subject,
            String topic,
            QuestionSource source) {
        Question question = new Question();
        question.createdBy = createdBy;
        question.stem = stem;
        question.questionType = questionType;
        question.subject = subject;
        question.topic = topic;
        question.difficulty = Difficulty.INTERMEDIATE;
        question.eloRating = DEFAULT_ELO;
        question.source = source;
        question.status = PublishStatus.PUBLISHED;
        return question;
    }

    public void addChoice(String label, String content, boolean correct, int position) {
        choices.add(QuestionChoice.create(this, label, content, correct, position));
    }

    public void classify(Difficulty difficulty, String bloomLevel, String tags, int eloRating) {
        this.difficulty = difficulty;
        this.bloomLevel = bloomLevel;
        this.tags = tags;
        this.eloRating = eloRating;
    }

    public void updateElo(int newRating) {
        this.eloRating = newRating;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setOfficialAnswer(String officialAnswer) {
        this.officialAnswer = officialAnswer;
    }

    public List<QuestionChoice> correctChoices() {
        return choices.stream().filter(QuestionChoice::isCorrect).toList();
    }
}
