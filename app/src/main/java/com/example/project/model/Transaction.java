package com.example.project.model;

public class Transaction {
    private int id;
    private int userId;
    private double amount;
    private String category;
    private String date;
    private String note;
    private String type;  // Thêm thuộc tính type
    private boolean isSynced;

    // Constructor đầy đủ
    public Transaction(int id, int userId, double amount, String category, String date, String note, String type, boolean isSynced) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.type = type;
        this.isSynced = isSynced;
    }
    public Transaction(int id, double amount, String category, String date, String note) {
        this.id = id;
        this.userId = -1; // Giá trị mặc định, cập nhật sau nếu cần
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.type = amount < 0 ? "expense" : "income"; // Mặc định dựa vào số tiền
        this.isSynced = false; // Mặc định chưa đồng bộ
    }


    // Constructor không có id (dành cho thêm mới)
    public Transaction(int userId, double amount, String category, String date, String note, String type) {
        this.userId = userId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
        this.type = type;
        this.isSynced = false;
    }

    // Getter & Setter cho type
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Các Getter khác
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public String getNote() {
        return note;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
