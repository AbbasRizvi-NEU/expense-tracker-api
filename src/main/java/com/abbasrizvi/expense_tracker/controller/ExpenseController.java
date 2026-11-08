package com.abbasrizvi.expense_tracker.controller;

import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import com.abbasrizvi.expense_tracker.service.ExpenseService;
import com.abbasrizvi.expense_tracker.service.IdempotentExpenseCreation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService service;
    private final IdempotentExpenseCreation expenseCreation;

    @Autowired
    public ExpenseController(ExpenseService service, IdempotentExpenseCreation expenseCreation) {
        this.service = service;
        this.expenseCreation = expenseCreation;
    }

    @PostMapping
    public ResponseEntity<Expense> createExpense(
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody Expense expense) {
        Expense created = expenseCreation.create(idempotencyKey, expense);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> getAllExpenses() {
        return ResponseEntity.ok(service.getAllExpenses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Expense> getExpenseById(@PathVariable Long id) {
        return service.getExpenseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Expense> updateExpense(
            @PathVariable Long id,
            @Valid @RequestBody Expense expense) {
        Expense updated = service.updateExpense(id, expense);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<Category, Double>> getSummary() {
        return ResponseEntity.ok(service.getSummaryByCategory());
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Application is running");
    }
}
