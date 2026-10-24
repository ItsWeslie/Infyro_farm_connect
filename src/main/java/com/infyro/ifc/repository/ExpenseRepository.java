package com.infyro.ifc.repository;

import com.infyro.ifc.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByFarm_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(Long farmId, String email);
    List<Expense> findAllByCropPlanting_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(Long plantingId, String email);
    Optional<Expense> findByIdAndFarm_Owner_Email(Long id, String email);
}
