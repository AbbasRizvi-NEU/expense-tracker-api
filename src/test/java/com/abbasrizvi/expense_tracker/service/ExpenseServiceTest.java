package com.abbasrizvi.expense_tracker.service;

import com.abbasrizvi.expense_tracker.exception.ResourceNotFoundException;
import com.abbasrizvi.expense_tracker.model.Category;
import com.abbasrizvi.expense_tracker.model.Expense;
import com.abbasrizvi.expense_tracker.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService service;

    private Expense testExpense;

    @BeforeEach
    void setUp() {
        testExpense = new Expense();
        testExpense.setId(1L);
        testExpense.setDescription("Test Expense");
        testExpense.setAmount(100.0);
        testExpense.setCategory(Category.FOOD);
        testExpense.setDate(LocalDate.now());
    }

    @Test
    void createExpense_ShouldReturnSavedExpense() {
        when(repository.save(any(Expense.class))).thenReturn(testExpense);

        Expense result = service.createExpense(testExpense);

        assertNotNull(result);
        assertEquals(testExpense.getId(), result.getId());
        verify(repository, times(1)).save(testExpense);
    }

    @Test
    void getAllExpenses_ShouldReturnList() {
        List<Expense> expenses = Arrays.asList(testExpense);
        when(repository.findAll()).thenReturn(expenses);

        List<Expense> result = service.getAllExpenses();

        assertEquals(1, result.size());
        verify(repository, times(1)).findAll();
    }

    @Test
    void getExpenseById_WhenExists_ShouldReturnExpense() {
        when(repository.findById(1L)).thenReturn(Optional.of(testExpense));

        Optional<Expense> result = service.getExpenseById(1L);

        assertTrue(result.isPresent());
        assertEquals(testExpense.getId(), result.get().getId());
    }

    @Test
    void getExpenseById_WhenNotExists_ShouldReturnEmpty() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        Optional<Expense> result = service.getExpenseById(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void updateExpense_WhenExists_ShouldReturnUpdated() {
        Expense updatedDetails = new Expense();
        updatedDetails.setDescription("Updated");
        updatedDetails.setAmount(200.0);
        updatedDetails.setCategory(Category.TRANSPORT);
        updatedDetails.setDate(LocalDate.now());

        when(repository.findById(1L)).thenReturn(Optional.of(testExpense));
        when(repository.save(any(Expense.class))).thenReturn(testExpense);

        Expense result = service.updateExpense(1L, updatedDetails);

        assertNotNull(result);
        verify(repository).save(any(Expense.class));
    }

    @Test
    void updateExpense_WhenNotExists_ShouldThrowException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            service.updateExpense(999L, testExpense);
        });
    }

    @Test
    void deleteExpense_WhenExists_ShouldCallRepository() {
        when(repository.existsById(1L)).thenReturn(true);
        doNothing().when(repository).deleteById(1L);

        service.deleteExpense(1L);

        verify(repository, times(1)).deleteById(1L);
    }

    @Test
    void deleteExpense_WhenNotExists_ShouldThrowException() {
        when(repository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            service.deleteExpense(999L);
        });
    }

    @Test
    void getSummaryByCategory_ShouldReturnMap() {
        Expense expense1 = new Expense();
        expense1.setCategory(Category.FOOD);
        expense1.setAmount(50.0);

        Expense expense2 = new Expense();
        expense2.setCategory(Category.FOOD);
        expense2.setAmount(30.0);

        Expense expense3 = new Expense();
        expense3.setCategory(Category.TRANSPORT);
        expense3.setAmount(20.0);

        when(repository.findAll()).thenReturn(Arrays.asList(expense1, expense2, expense3));

        Map<Category, Double> summary = service.getSummaryByCategory();

        assertEquals(80.0, summary.get(Category.FOOD));
        assertEquals(20.0, summary.get(Category.TRANSPORT));
    }
}