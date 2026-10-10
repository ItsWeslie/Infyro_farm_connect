package com.infyro.ifc.repository;

import com.infyro.ifc.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FieldRepo extends JpaRepository<Field,Long> {

}
