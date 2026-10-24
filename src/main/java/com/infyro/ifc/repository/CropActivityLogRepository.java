package com.infyro.ifc.repository;

import com.infyro.ifc.model.CropActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropActivityLogRepository extends JpaRepository<CropActivityLog, Long> {

    List<CropActivityLog> findAllByCropPlanting_IdAndCropPlanting_Field_Farm_Owner_EmailOrderByDayNumberAscCreatedAtAsc(
            Long plantingId, String email);

    Optional<CropActivityLog> findByIdAndCropPlanting_Field_Farm_Owner_Email(
            Long id, String email);
}
