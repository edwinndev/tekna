package com.tekna.api.repository;

import com.tekna.api.entity.Alert;
import com.tekna.api.entity.AlertStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AlertRepository extends JpaRepository<Alert, UUID>, JpaSpecificationExecutor<Alert> {

    boolean existsByProductIdAndStatus(UUID productId, AlertStatus status);

    Optional<Alert> findTopByProductIdAndStatusOrderByCreatedAtDesc(UUID productId, AlertStatus status);
}
