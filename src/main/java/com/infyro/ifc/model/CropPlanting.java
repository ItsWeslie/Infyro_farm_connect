package com.infyro.ifc.model;

import com.infyro.ifc.enums.CropPlantingStatus;
import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "crop_plantings",
        indexes = {
                @Index(name = "idx_crop_planting_field_id", columnList = "field_id"),
                @Index(name = "idx_crop_planting_crop_type_id", columnList = "crop_type_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CropPlanting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "field_id", nullable = false)
    private Field field;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_type_id", nullable = false)
    private CropType cropType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cropArea;

    @Column(nullable = false)
    private LocalDate plantedOn;

    private LocalDate expectedHarvestOn;

    private LocalDate completedOn;

    @Column(nullable = false)
    private Integer maturityTimeInDays;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CropPlantingStatus status = CropPlantingStatus.PLANNED;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}