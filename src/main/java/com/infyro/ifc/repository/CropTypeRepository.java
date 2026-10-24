package com.infyro.ifc.repository;

import com.infyro.ifc.model.CropType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropTypeRepository extends JpaRepository<CropType, Long> {

    List<CropType> findAllByFarm_IdAndFarm_Owner_Email(Long farmId, String ownerEmail);

    Optional<CropType> findByIdAndFarm_IdAndFarm_Owner_Email(
            Long id,
            Long farmId,
            String ownerEmail
    );

    boolean existsByFarm_IdAndNameIgnoreCase(
            Long farmId,
            String name
    );

    boolean existsByFarm_IdAndNameIgnoreCaseAndIdNot(
            Long farmId, String name, Long id);
}
