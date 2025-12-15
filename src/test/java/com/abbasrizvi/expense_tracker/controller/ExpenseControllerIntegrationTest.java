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

import static org.junit.jupiter.api.Assertions.assertFalse;
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