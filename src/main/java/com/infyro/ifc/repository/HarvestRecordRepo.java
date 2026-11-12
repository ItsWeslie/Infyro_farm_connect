package com.infyro.ifc.repository;

import com.infyro.ifc.enums.HarvestUnit;
import com.infyro.ifc.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HarvestRecordRepo extends JpaRepository<HarvestRecord, Long> {

    @Query("""
        SELECT h
        FROM HarvestRecord h
        WHERE h.cropPlanting.id = :plantingId
          AND h.cropPlanting.field.farm.owner.email = :email
        ORDER BY h.harvestDate DESC, h.createdAt DESC
    """)
    List<HarvestRecord> findByPlanting(@Param("plantingId") Long plantingId,
                                       @Param("email") String email);

    @Query("""
        SELECT h.unit AS unit, SUM(h.quantity) AS total, COUNT(h) AS recordCount
        FROM HarvestRecord h
        WHERE h.cropPlanting.field.farm.id = :farmId
          AND h.cropPlanting.field.farm.owner.email = :email
          AND h.voided = false
          AND (:startDate IS NULL OR h.harvestDate >= :startDate)
          AND (:endDate IS NULL OR h.harvestDate <= :endDate)
        GROUP BY h.unit
    """)
    List<UnitTotal> summarizeByFarm(@Param("farmId") Long farmId,
                                    @Param("email") String email,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    Optional<HarvestRecord> findByIdAndCropPlanting_Field_Farm_Owner_Email(Long id, String email);

    boolean existsByCropPlanting_Id(Long plantingId);
    boolean existsByCropPlanting_Field_Farm_Id(Long farmId);

    interface UnitTotal {
        HarvestUnit getUnit();
        java.math.BigDecimal getTotal();
        Long getRecordCount();
    }
}
