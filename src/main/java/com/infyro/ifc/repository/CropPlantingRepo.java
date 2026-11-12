package com.infyro.ifc.repository;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CropPlantingRepo extends JpaRepository<CropPlanting, Long> {

    boolean existsByCropType_Id(Long cropTypeId);
    List<CropPlanting> findAllByField_Farm_Owner_Email(String email);
    List<CropPlanting> findAllByField_IdAndField_Farm_Owner_Email(Long fieldId, String email);
    List<CropPlanting> findAllByField_Farm_IdAndField_Farm_Owner_Email(Long farmId, String email);
    Optional<CropPlanting> findByIdAndField_Farm_Owner_Email(Long id, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT p FROM CropPlanting p
    WHERE p.id = :id
      AND p.field.farm.owner.email = :email
    """)
    Optional<CropPlanting> findOwnedPlantingForUpdate(@Param("id") Long id, @Param("email") String email);

    @Query("""
            select p from CropPlanting p
            join fetch p.field f
            join fetch f.farm farm
            join fetch p.cropType crop
            where farm.owner.email = :email
              and ((p.plantedOn between :fromDate and :toDate)
                or (p.expectedHarvestOn between :fromDate and :toDate))
            order by p.plantedOn
            """)
    List<CropPlanting> findCalendarPlantings(
            @Param("email") String email,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);

    @Query("""
            select p from CropPlanting p
            join fetch p.field f
            join fetch f.farm farm
            join fetch farm.owner owner
            join fetch p.cropType crop
            where ((p.plantedOn = :eventDate and p.status = :plannedStatus)
                or (p.expectedHarvestOn = :eventDate and p.status in :harvestStatuses))
            """)
    List<CropPlanting> findReminderCandidates(
            @Param("eventDate") LocalDate eventDate,
            @Param("plannedStatus") CropPlantingStatus plannedStatus,
            @Param("harvestStatuses") Collection<CropPlantingStatus> harvestStatuses);
}
