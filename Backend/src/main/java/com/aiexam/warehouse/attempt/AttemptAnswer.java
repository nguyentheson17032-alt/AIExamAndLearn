package com.aiexam.warehouse.attempt;

import com.aiexam.warehouse.question.Question;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "attempt_answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    @Column(name = "selected_choice_ids", columnDefinition = "TEXT")
    private String selectedChoiceIds;

    @Column
    private Boolean correct;

    @Column(name = "score_awarded", precision = 8, scale = 2)
    private BigDecimal scoreAwarded;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    public static AttemptAnswer create(Attempt attempt, Question question) {
        AttemptAnswer answer = new AttemptAnswer();
        answer.attempt = attempt;
        answer.question = question;
        return answer;
    }

    public void recordResponse(String answerText, String selectedChoiceIds) {
        this.answerText = answerText;
        this.selectedChoiceIds = selectedChoiceIds;
    }

    public void grade(boolean correct, BigDecimal scoreAwarded, String aiFeedback) {
        this.correct = correct;
        this.scoreAwarded = scoreAwarded;
        this.aiFeedback = aiFeedback;
    }
}
