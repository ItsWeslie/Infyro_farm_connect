package com.infyro.ifc.repository;

import com.infyro.ifc.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface HarvestRecordRepository extends JpaRepository<HarvestRecord, Long> {
    List<HarvestRecord> findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(Long plantingId, String email);
    List<HarvestRecord> findAllByCropPlanting_Field_Farm_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByHarvestDateDescCreatedAtDesc(Long farmId, String email);
    Optional<HarvestRecord> findByIdAndCropPlanting_Field_Farm_Owner_Email(Long id, String email);
    boolean existsByCropPlanting_Id(Long plantingId);
    boolean existsByCropPlanting_Field_Farm_Id(Long farmId);
}
