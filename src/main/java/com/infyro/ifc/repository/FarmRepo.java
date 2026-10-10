package com.infyro.ifc.repository;

import com.infyro.ifc.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FarmRepo extends JpaRepository<Farm,Long> {

}
