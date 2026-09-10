package com.aiexam.warehouse.elo;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EloHistoryRepository extends JpaRepository<EloHistory, UUID> {

    Page<EloHistory> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
}
