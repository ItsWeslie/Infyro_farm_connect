package com.infyro.ifc.repository;

import com.infyro.ifc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserEmailRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
}
