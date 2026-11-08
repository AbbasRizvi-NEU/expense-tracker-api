package com.abbasrizvi.expense_tracker.service;

import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import com.abbasrizvi.expense_tracker.model.ExpenseCreationReceipt;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Service
public class IdempotentExpenseCreation {

    private static final Pattern VALID_KEY = Pattern.compile("[!-~]{1,128}");

    private final ExpenseService expenses;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transaction;

    public IdempotentExpenseCreation(ExpenseService expenses, EntityManager entityManager,
                                     ObjectMapper objectMapper, PlatformTransactionManager transactionManager) {
        this.expenses = expenses;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.transaction = new TransactionTemplate(transactionManager);
    }

    public Expense create(String key, Expense expense) {
        if (key == null) {
            return expenses.createExpense(expense);
        }
        if (!VALID_KEY.matcher(key).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Idempotency-Key must contain 1 to 128 printable ASCII characters without spaces");
        }

        ExpenseInput input = new ExpenseInput(expense.getDescription(), expense.getAmount(),
                expense.getCategory(), expense.getDate());
        String requestBody = writeJson(input);
        try {
            return transaction.execute(status -> {
                ExpenseCreationReceipt previous = entityManager.find(ExpenseCreationReceipt.class, key);
                if (previous != null) {
                    return replay(previous, requestBody);
                }

                Expense created = expenses.createExpense(
                        new Expense(input.description(), input.amount(), input.category(), input.date()));
                entityManager.persist(new ExpenseCreationReceipt(key, requestBody, writeJson(created)));
                return created;
            });
        } catch (DataIntegrityViolationException failure) {
            // A competing request can commit the receipt while this transaction is still open.
            // The unique key rolls back this request's expense along with its receipt.
            ExpenseCreationReceipt winner = entityManager.find(ExpenseCreationReceipt.class, key);
            if (winner == null) {
                throw failure;
            }
            return replay(winner, requestBody);
        }
    }

    private Expense replay(ExpenseCreationReceipt receipt, String requestBody) {
        if (!receipt.getRequestBody().equals(requestBody)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Idempotency-Key was already used for a different expense");
        }
        try {
            return objectMapper.readValue(receipt.getResponseBody(), Expense.class);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("Could not read the stored expense response", failure);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException failure) {
            throw new IllegalStateException("Could not serialize the expense", failure);
        }
    }

    private record ExpenseInput(String description, Double amount, Category category, LocalDate date) {
    }
}
