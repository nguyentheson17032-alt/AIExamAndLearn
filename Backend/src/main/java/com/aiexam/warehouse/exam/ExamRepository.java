package com.aiexam.warehouse.exam;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {

    @EntityGraph(attributePaths = {"questions", "questions.question", "questions.question.choices"})
    Optional<Exam> findWithQuestionsById(UUID id);

    @EntityGraph(attributePaths = {"createdBy", "questions"})
    Page<Exam> findByStatus(ExamStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "questions"})
    Page<Exam> findByStatusAndExamType(ExamStatus status, ExamType examType, Pageable pageable);
}
