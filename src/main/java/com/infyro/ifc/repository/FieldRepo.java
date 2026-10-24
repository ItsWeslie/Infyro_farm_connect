package com.infyro.ifc.repository;

import com.infyro.ifc.model.Field;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FieldRepo extends JpaRepository<Field,Long> {
    List<Field> findAllByFarm_IdAndFarm_Owner_Email(
            Long farmId, String email);

    Optional<Field> findByIdAndFarm_Owner_Email(
            Long fieldId, String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT f FROM Field f
    WHERE f.id = :fieldId
      AND f.farm.owner.email = :email
    """)
    Optional<Field> findOwnedFieldForUpdate(@Param("fieldId") Long fieldId,
                                            @Param("email") String email);
}
