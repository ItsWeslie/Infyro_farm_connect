package com.infyro.ifc.model;

import com.infyro.ifc.enums.CropType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "crops", indexes = {
        @Index(name = "idx_crop_field_id", columnList = "field_id")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CropType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            referencedColumnName = "id",
            name = "field_id",
            nullable = false
    )
    private Field field;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cropArea;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal expense;

    @Column(nullable = false)
    private LocalDate plantedOn;

    @Column(nullable = false)
    private LocalDate cultivateOn;

    private Integer maturityTimeInDays;
}