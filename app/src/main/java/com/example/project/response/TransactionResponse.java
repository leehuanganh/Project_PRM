package com.example.project.response;

import com.example.project.model.Transaction;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("transactions")
    private List<Transaction> transactions;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }
}
