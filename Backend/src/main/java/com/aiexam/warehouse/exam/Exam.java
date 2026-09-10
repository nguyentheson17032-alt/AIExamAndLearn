package com.aiexam.warehouse.exam;

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
@Table(name = "exams", indexes = {
        @Index(name = "idx_exams_created_by_id", columnList = "created_by_id"),
        @Index(name = "idx_exams_exam_type", columnList = "exam_type"),
        @Index(name = "idx_exams_status", columnList = "status"),
        @Index(name = "idx_exams_target_elo", columnList = "target_elo")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 50)
    private ExamType examType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExamSource source;

    @Column(name = "target_elo")
    private Integer targetElo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ExamStatus status;

    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<ExamQuestion> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Version
    private Long version;

    public static Exam create(
            User createdBy,
            String title,
            String description,
            ExamType examType,
            ExamSource source,
            Integer targetElo,
            Integer timeLimitMinutes) {
        Exam exam = new Exam();
        exam.createdBy = createdBy;
        exam.title = title;
        exam.description = description;
        exam.examType = examType;
        exam.source = source;
        exam.targetElo = targetElo;
        exam.status = ExamStatus.PUBLISHED;
        exam.timeLimitMinutes = timeLimitMinutes;
        return exam;
    }

    public void addQuestion(Question question, int points) {
        int position = questions.size() + 1;
        questions.add(ExamQuestion.create(this, question, position, points));
    }

    public int maxScore() {
        return questions.stream().mapToInt(ExamQuestion::getPoints).sum();
    }
}
