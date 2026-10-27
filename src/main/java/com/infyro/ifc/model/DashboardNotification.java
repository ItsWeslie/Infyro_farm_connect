package com.infyro.ifc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "dashboard_notifications",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_owner_event",
                columnNames = {"owner_id", "event_key"}),
        indexes = @Index(name = "idx_notification_owner_scheduled", columnList = "owner_id, scheduled_at"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "event_key", nullable = false, length = 180)
    private String eventKey;

    @Column(nullable = false, length = 40)
    private String eventType;

    @Column(nullable = false, length = 240)
    private String title;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean read = false;

    private Instant readAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
