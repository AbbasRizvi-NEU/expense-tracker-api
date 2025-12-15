package com.abbasrizvi.expense_tracker.repository;

import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByCategory(Category category);
    List<Expense> findByDateBetween(LocalDate start, LocalDate end);
}