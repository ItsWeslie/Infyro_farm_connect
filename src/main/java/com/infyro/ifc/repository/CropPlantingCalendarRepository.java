package com.infyro.ifc.repository;

import com.infyro.ifc.model.CropPlanting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CropPlantingCalendarRepository extends JpaRepository<CropPlanting, Long> {
    @Query("""
        select cp from CropPlanting cp
        join cp.field f
        join f.farm farm
        join farm.owner owner
        where lower(owner.email) = lower(:email)
          and cp.status <> com.infyro.ifc.enums.CropPlantingStatus.CANCELLED
          and ((cp.plantedOn between :fromDate and :toDate)
            or (cp.expectedHarvestOn between :fromDate and :toDate))
        order by cp.plantedOn asc, cp.id asc
        """)
    List<CropPlanting> findCalendarPlantings(@Param("email") String email,
                                              @Param("fromDate") LocalDate from,
                                              @Param("toDate") LocalDate to);
}
