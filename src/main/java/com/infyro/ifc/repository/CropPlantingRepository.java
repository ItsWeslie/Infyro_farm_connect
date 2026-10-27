package com.infyro.ifc.repository;

import com.infyro.ifc.enums.CropPlantingStatus;
import com.infyro.ifc.model.CropPlanting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface CropPlantingRepository extends JpaRepository<CropPlanting, Long> {

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