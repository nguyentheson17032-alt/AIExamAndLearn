package com.aiexam.warehouse.attempt;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {

    @EntityGraph(attributePaths = {
            "answers",
            "answers.question",
            "answers.question.choices",
            "exam",
            "exam.questions",
            "user"
    })
    Optional<Attempt> findWithDetailsById(UUID id);

    @EntityGraph(attributePaths = {"exam", "answers", "answers.question"})
    Page<Attempt> findByUser_Id(UUID userId, Pageable pageable);

    boolean existsByUser_IdAndExam_IdAndStatus(UUID userId, UUID examId, AttemptStatus status);
}
