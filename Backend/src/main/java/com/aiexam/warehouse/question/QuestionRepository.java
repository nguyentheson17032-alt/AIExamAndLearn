package com.aiexam.warehouse.question;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    @EntityGraph(attributePaths = {"choices", "createdBy"})
    Optional<Question> findWithChoicesById(UUID id);

    @EntityGraph(attributePaths = {"choices"})
    Page<Question> findByStatus(PublishStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"choices"})
    Page<Question> findByStatusAndSubject(PublishStatus status, String subject, Pageable pageable);

    @Query("""
            SELECT q FROM Question q
            WHERE q.status = :status
              AND q.subject = :subject
              AND q.eloRating BETWEEN :minElo AND :maxElo
            """)
    List<Question> findPublishedNearElo(
            @Param("status") PublishStatus status,
            @Param("subject") String subject,
            @Param("minElo") int minElo,
            @Param("maxElo") int maxElo);
}
