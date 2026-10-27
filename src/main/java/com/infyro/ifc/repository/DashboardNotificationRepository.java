package com.infyro.ifc.repository;

import com.infyro.ifc.model.DashboardNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DashboardNotificationRepository extends JpaRepository<DashboardNotification, Long> {
    List<DashboardNotification> findAllByOwner_EmailOrderByScheduledAtDesc(String email, Pageable pageable);
    List<DashboardNotification> findAllByOwner_EmailAndReadFalseOrderByScheduledAtDesc(String email, Pageable pageable);
    Optional<DashboardNotification> findByIdAndOwner_Email(Long id, String email);
    long countByOwner_EmailAndReadFalse(String email);
    boolean existsByOwner_IdAndEventKey(Long ownerId, String eventKey);
}