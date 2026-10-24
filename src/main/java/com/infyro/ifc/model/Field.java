package com.infyro.ifc.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fields", indexes = {
        @Index(name = "idx_field_name", columnList = "fieldName"),
        @Index(name = "idx_field_farm_id", columnList = "farm_id")
})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Field {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false)
    private String fieldName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalArea;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal availableArea;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            referencedColumnName = "id",
            name = "farm_id",
            nullable = false
    )
    private Farm farm;

    @Builder.Default
    @OneToMany(mappedBy = "field",
            fetch = FetchType.LAZY)
    private List<CropPlanting> crops = new ArrayList<>();
}