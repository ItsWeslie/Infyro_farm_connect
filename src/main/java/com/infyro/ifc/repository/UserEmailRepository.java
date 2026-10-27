package com.infyro.ifc.repository;

import com.infyro.ifc.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserEmailRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

    @Query("select distinct u from User u join u.farms farm")
    List<User> findAllUsersWithFarms();
}
