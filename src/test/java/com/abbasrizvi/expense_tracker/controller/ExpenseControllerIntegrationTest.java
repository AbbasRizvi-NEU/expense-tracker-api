package com.abbasrizvi.expense_tracker.controller;

import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import com.abbasrizvi.expense_tracker.repository.ExpenseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ExpenseRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void healthCheck_ShouldReturn200() throws Exception {
        mockMvc.perform(get("/api/expenses/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Application is running"));
    }

    @Test
    void createExpense_WithValidData_ShouldReturn201() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test Expense");
        expense.setAmount(100.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Test Expense"))
                .andExpect(jsonPath("$.amount").value(100.0));
    }

    @Test
    void createExpense_WithInvalidData_ShouldReturn400() throws Exception {
        Expense expense = new Expense();
        // Missing required fields

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createExpense_WithSameKeyAndPayload_ShouldReplayOriginalResponse() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = expenseBody(25.50);
        String original = postExpense(key, body);

        assertEquals(original, postExpense(key, body));
        assertEquals(1, repository.count());
        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(jsonPath("$.FOOD").value(25.50));
    }

    @Test
    void createExpense_WithSameKeyAndDifferentPayload_ShouldReturn409() throws Exception {
        String key = UUID.randomUUID().toString();
        postExpense(key, expenseBody(25.50));

        mockMvc.perform(post("/api/expenses")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseBody(75.00)))
                .andExpect(status().isConflict());

        assertEquals(1, repository.count());
        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(jsonPath("$.FOOD").value(25.50));
    }

    @Test
    void createExpense_WithConcurrentRetries_ShouldCreateOneExpense() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = expenseBody(25.50);
        var pool = Executors.newFixedThreadPool(2);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try {
            var first = pool.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return postExpense(key, body);
            });
            var second = pool.submit(() -> {
                ready.countDown();
                assertTrue(start.await(10, TimeUnit.SECONDS));
                return postExpense(key, body);
            });

            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            assertEquals(first.get(15, TimeUnit.SECONDS), second.get(15, TimeUnit.SECONDS));
            assertEquals(1, repository.count());
            mockMvc.perform(get("/api/expenses/summary"))
                    .andExpect(jsonPath("$.FOOD").value(25.50));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void createExpense_AfterDeletingOriginal_ShouldReplayWithoutRecreatingExpense() throws Exception {
        String key = UUID.randomUUID().toString();
        String body = expenseBody(25.50);
        String original = postExpense(key, body);
        long id = objectMapper.readTree(original).get("id").asLong();
        mockMvc.perform(delete("/api/expenses/{id}", id)).andExpect(status().isNoContent());

        assertEquals(original, postExpense(key, body));
        assertEquals(0, repository.count());
    }

    @Test
    void createExpense_WithInvalidKey_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/expenses")
                        .header("Idempotency-Key", "x".repeat(129))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(expenseBody(25.50)))
                .andExpect(status().isBadRequest());
        assertEquals(0, repository.count());
    }

    private String expenseBody(double amount) throws Exception {
        return objectMapper.writeValueAsString(
                new Expense("Lunch", amount, Category.FOOD, LocalDate.of(2025, 12, 15)));
    }

    private String postExpense(String key, String body) throws Exception {
        return mockMvc.perform(post("/api/expenses")
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void createExpense_WithNegativeAmount_ShouldReturn400() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Invalid");
        expense.setAmount(-50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllExpenses_ShouldReturnList() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test");
        expense.setAmount(50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());
        repository.save(expense);

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].description").value("Test"));
    }

    @Test
    void getAllExpenses_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getExpenseById_WhenExists_ShouldReturn200() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test");
        expense.setAmount(50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());
        Expense saved = repository.save(expense);

        mockMvc.perform(get("/api/expenses/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()));
    }

    @Test
    void getExpenseById_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateExpense_WhenExists_ShouldReturn200() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Original");
        expense.setAmount(50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());
        Expense saved = repository.save(expense);

        saved.setDescription("Updated");
        saved.setAmount(75.0);

        mockMvc.perform(put("/api/expenses/" + saved.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saved)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.amount").value(75.0));
    }

    @Test
    void updateExpense_WhenNotExists_ShouldReturn404() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test");
        expense.setAmount(50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());

        mockMvc.perform(put("/api/expenses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expense)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteExpense_WhenExists_ShouldReturn204() throws Exception {
        Expense expense = new Expense();
        expense.setDescription("Test");
        expense.setAmount(50.0);
        expense.setCategory(Category.FOOD);
        expense.setDate(LocalDate.now());
        Expense saved = repository.save(expense);

        mockMvc.perform(delete("/api/expenses/" + saved.getId()))
                .andExpect(status().isNoContent());

        assertFalse(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void deleteExpense_WhenNotExists_ShouldReturn404() throws Exception {
        mockMvc.perform(delete("/api/expenses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getSummary_ShouldReturnCategorySummary() throws Exception {
        Expense expense1 = new Expense();
        expense1.setDescription("Lunch");
        expense1.setAmount(50.0);
        expense1.setCategory(Category.FOOD);
        expense1.setDate(LocalDate.now());

        Expense expense2 = new Expense();
        expense2.setDescription("Bus");
        expense2.setAmount(20.0);
        expense2.setCategory(Category.TRANSPORT);
        expense2.setDate(LocalDate.now());

        repository.save(expense1);
        repository.save(expense2);

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.FOOD").value(50.0))
                .andExpect(jsonPath("$.TRANSPORT").value(20.0));
    }

    @Test
    void getSummary_WhenEmpty_ShouldReturnEmptyMap() throws Exception {
        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
