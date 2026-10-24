package com.infyro.ifc.model;

import com.infyro.ifc.enums.HarvestUnit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "harvest_records", indexes = {
        @Index(name = "idx_harvest_planting_date", columnList = "crop_planting_id, harvestDate")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class HarvestRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "crop_planting_id", nullable = false)
    private CropPlanting cropPlanting;

    @Column(nullable = false)
    private LocalDate harvestDate;

    @Column(nullable = false, precision = 14, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HarvestUnit unit;

    @Column(length = 2000)
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private boolean voided = false;

    private Instant voidedAt;

    @Column(length = 500)
    private String voidReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
