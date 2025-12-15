package com.abbasrizvi.expense_tracker.service;

import com.abbasrizvi.expense_tracker.exception.ResourceNotFoundException;
import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import com.abbasrizvi.expense_tracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    private final ExpenseRepository repository;

    @Autowired
    public ExpenseService(ExpenseRepository repository) {
        this.repository = repository;
    }

    public Expense createExpense(Expense expense) {
        return repository.save(expense);
    }

    public List<Expense> getAllExpenses() {
        return repository.findAll();
    }

    public Optional<Expense> getExpenseById(Long id) {
        return repository.findById(id);
    }

    public Expense updateExpense(Long id, Expense expenseDetails) {
        Expense expense = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with id: " + id));

        expense.setDescription(expenseDetails.getDescription());
        expense.setAmount(expenseDetails.getAmount());
        expense.setCategory(expenseDetails.getCategory());
        expense.setDate(expenseDetails.getDate());

        return repository.save(expense);
    }

    public void deleteExpense(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Expense not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public Map<Category, Double> getSummaryByCategory() {
        List<Expense> expenses = repository.findAll();
        return expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));
    }
}
