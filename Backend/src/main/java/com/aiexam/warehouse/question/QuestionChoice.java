package com.aiexam.warehouse.question;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "question_choices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 8)
    private String label;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int position;

    public static QuestionChoice create(Question question, String label, String content, boolean correct, int position) {
        QuestionChoice choice = new QuestionChoice();
        choice.question = question;
        choice.label = label;
        choice.content = content;
        choice.correct = correct;
        choice.position = position;
        return choice;
    }
}
