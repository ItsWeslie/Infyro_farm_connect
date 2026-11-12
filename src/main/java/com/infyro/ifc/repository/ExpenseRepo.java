package com.infyro.ifc.repository;

import com.infyro.ifc.enums.ExpenseCategory;
import com.infyro.ifc.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepo extends JpaRepository<Expense, Long> {

    @Query("""
        SELECT e
        FROM Expense e
        WHERE e.farm.id = :farmId
          AND e.farm.owner.email = :email
        ORDER BY e.expenseDate DESC, e.createdAt DESC
    """)
    List<Expense> findByFarm(@Param("farmId") Long farmId,
                             @Param("email") String email);

    List<Expense> findAllByCropPlanting_IdAndFarm_Owner_EmailOrderByExpenseDateDescCreatedAtDesc(
            Long plantingId, String email);

    @Query("""
        SELECT e.category AS category, SUM(e.amount) AS total, COUNT(e) AS recordCount
        FROM Expense e
        WHERE e.farm.id = :farmId
          AND e.farm.owner.email = :email
          AND e.voided = false
          AND (:from IS NULL OR e.expenseDate >= :from)
          AND (:to IS NULL OR e.expenseDate <= :to)
        GROUP BY e.category
    """)
    List<CategoryTotal> summarizeByFarm(@Param("farmId") Long farmId,
                                        @Param("email") String email,
                                        @Param("from") LocalDate from,
                                        @Param("to") LocalDate to);

    Optional<Expense> findByIdAndFarm_Owner_Email(Long id, String email);

    boolean existsByFarm_Id(Long farmId);
    boolean existsByCropPlanting_Id(Long plantingId);

    interface CategoryTotal {
        ExpenseCategory getCategory();
        java.math.BigDecimal getTotal();
        Long getRecordCount();
    }
}
