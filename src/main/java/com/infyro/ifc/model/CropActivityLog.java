package com.infyro.ifc.model;

import com.infyro.ifc.enums.ActivityStatus;
import com.infyro.ifc.enums.CropStage;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crop_activity_logs", indexes = {
        @Index(name = "idx_activity_planting_id", columnList = "crop_planting_id"),
        @Index(name = "idx_activity_planting_day", columnList = "crop_planting_id, dayNumber")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_planting_id", nullable = false)
    private CropPlanting cropPlanting;

    @Column(nullable = false)
    private Integer dayNumber;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private CropStage cropStage;

    @Column(nullable = false, length = 2000)
    private String dailyActivity;

    @Column(length = 2000)
    private String nutritionAndWaterManagement;

    @Column(length = 2000)
    private String pestAndDiseaseControl;

    @Column(length = 2000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ActivityStatus status = ActivityStatus.PLANNED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
