package com.infyro.ifc.repository;

import com.infyro.ifc.model.Field;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldRepo extends JpaRepository<Field,Long> {
    List<Field> findAllByFarm_IdAndFarm_Owner_Email(
            Long farmId, String email);

    Optional<Field> findByIdAndFarm_Owner_Email(
            Long fieldId, String email);
}
