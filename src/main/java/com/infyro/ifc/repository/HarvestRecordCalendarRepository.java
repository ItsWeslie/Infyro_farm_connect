package com.infyro.ifc.repository;

import com.infyro.ifc.model.HarvestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HarvestRecordCalendarRepository extends JpaRepository<HarvestRecord, Long> {
    @Query("""
        select hr from HarvestRecord hr
        join hr.cropPlanting cp
        join cp.field f
        join f.farm farm
        join farm.owner owner
        where lower(owner.email) = lower(:email)
          and hr.voided = false
          and hr.harvestDate between :fromDate and :toDate
        order by hr.harvestDate asc, hr.id asc
        """)
    List<HarvestRecord> findCalendarHarvests(@Param("email") String email,
                                              @Param("fromDate") LocalDate from,
                                              @Param("toDate") LocalDate to);
}
