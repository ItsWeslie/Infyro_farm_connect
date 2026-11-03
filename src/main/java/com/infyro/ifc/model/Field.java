package com.infyro.ifc.model;

import com.infyro.ifc.enums.CropPlantingStatus;
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

    /*
     * Compatibility column for databases created before area was derived.
     * It is not used to calculate available area and should be removed by a
     * database migration after existing installations have migrated.
     */
    @Deprecated
    @Column(name = "availableArea", nullable = false, precision = 10, scale = 2)
    private BigDecimal legacyAvailableArea;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "farm_id", nullable = false)
    private Farm farm;

    @Builder.Default
    @OneToMany(mappedBy = "field", fetch = FetchType.LAZY)
    private List<CropPlanting> crops = new ArrayList<>();

    @Transient
    public BigDecimal getAvailableArea() {
        BigDecimal reservedArea = crops == null ? BigDecimal.ZERO :
                crops.stream()
                        .filter(planting -> planting.getStatus() == CropPlantingStatus.PLANNED
                                || planting.getStatus() == CropPlantingStatus.ACTIVE
                                || planting.getStatus() == CropPlantingStatus.HARVESTING)
                        .map(CropPlanting::getCropArea)
                        .filter(area -> area != null)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal available = totalArea == null
                ? BigDecimal.ZERO
                : totalArea.subtract(reservedArea);

        return available.max(BigDecimal.ZERO);
    }
}
