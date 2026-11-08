package com.abbasrizvi.expense_tracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "expense_creation_receipts")
public class ExpenseCreationReceipt {

    @Id
    @Column(name = "request_key", length = 128)
    private String key;

    @Column(nullable = false, columnDefinition = "text")
    private String requestBody;

    @Column(nullable = false, columnDefinition = "text")
    private String responseBody;

    protected ExpenseCreationReceipt() {
    }

    public ExpenseCreationReceipt(String key, String requestBody, String responseBody) {
        this.key = key;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
