package com.infyro.ifc.repository;

import com.infyro.ifc.model.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FarmRepo extends JpaRepository<Farm,Long> {

    List<Farm> findAllByOwner_Email(String email);

    Optional<Farm> findByIdAndOwner_Email(Long id, String email);

    boolean existsByIdAndOwner_Email(Long id, String email);

    void deleteByIdAndOwner_Email(Long id, String email);
}
