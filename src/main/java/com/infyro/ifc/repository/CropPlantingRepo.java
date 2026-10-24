package com.infyro.ifc.repository;

import com.infyro.ifc.model.CropPlanting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CropPlantingRepo extends JpaRepository<CropPlanting, Long> {
    boolean existsByCropType_Id(Long cropTypeId);

    List<CropPlanting> findAllByField_Farm_Owner_Email(String email);

    List<CropPlanting> findAllByField_IdAndField_Farm_Owner_Email(Long fieldId, String email);

    Optional<CropPlanting> findByIdAndField_Farm_Owner_Email(Long id, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT p FROM CropPlanting p
    WHERE p.id = :id
      AND p.field.farm.owner.email = :email
    """)
    Optional<CropPlanting> findOwnedPlantingForUpdate(
            @Param("id") Long id,
            @Param("email") String email);

}
