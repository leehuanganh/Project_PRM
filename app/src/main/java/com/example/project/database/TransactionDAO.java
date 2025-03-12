package com.example.project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.project.model.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    private SQLiteDatabase db;

    // Constants cho bảng transactions
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_IS_SYNCED = "is_synced";

    public TransactionDAO(Context context) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // 🔹 Thêm giao dịch mới
    public boolean addTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, transaction.getUserId());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());
        values.put(COLUMN_NOTE, transaction.getNote());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_IS_SYNCED, transaction.isSynced() ? 1 : 0);

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        return result != -1;
    }

    // 🔹 Lấy danh sách giao dịch
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY " + COLUMN_DATE + " DESC", null);

        if (cursor.moveToFirst()) {
            do {
                transactionList.add(createTransactionFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    // 🔹 Cập nhật giao dịch
    public boolean updateTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());
        values.put(COLUMN_NOTE, transaction.getNote());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_IS_SYNCED, transaction.isSynced() ? 1 : 0);

        int result = db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_ID + "=?", new String[]{String.valueOf(transaction.getId())});
        return result > 0;
    }

    // 🔹 Xóa giao dịch
    public boolean deleteTransaction(int id) {
        int result = db.delete(TABLE_TRANSACTIONS, COLUMN_TRANSACTION_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    // 🔹 Lấy danh sách giao dịch chưa đồng bộ
    public List<Transaction> getUnsyncedTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_IS_SYNCED + "=0", null);

        if (cursor.moveToFirst()) {
            do {
                transactionList.add(createTransactionFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    // 🔹 Cập nhật trạng thái đồng bộ
    public void updateSyncStatus(int transactionId, boolean isSynced) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SYNCED, isSynced ? 1 : 0);
        db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_ID + "=?", new String[]{String.valueOf(transactionId)});
    }

    // 🔹 Lấy tổng chi tiêu trong một ngày
    public float getTotalExpenseForDate(String date) {
        return getTotalForCondition(COLUMN_DATE + " = ?", new String[]{date});
    }

    // 🔹 Lấy tổng chi tiêu trong một tuần
    public float getTotalExpenseForWeek(int week) {
        return getTotalForCondition("strftime('%W', " + COLUMN_DATE + ") = ?", new String[]{String.valueOf(week)});
    }

    // 🔹 Lấy tổng chi tiêu trong một tháng
    public float getTotalExpenseForMonth(int month) {
        return getTotalForCondition("strftime('%m', " + COLUMN_DATE + ") = ?", new String[]{String.format("%02d", month)});
    }

    // 🔹 Lấy tổng chi tiêu trong một quý (3 tháng)
    public float getTotalExpenseForQuarter(int quarter) {
        int startMonth = (quarter - 1) * 3 + 1;
        int endMonth = startMonth + 2;
        return getTotalForCondition("strftime('%m', " + COLUMN_DATE + ") BETWEEN ? AND ?",
                new String[]{String.format("%02d", startMonth), String.format("%02d", endMonth)});
    }

    // 🔹 Lấy tổng chi tiêu trong một năm
    public float getTotalExpenseForYear(int year) {
        return getTotalForCondition("strftime('%Y', " + COLUMN_DATE + ") = ?", new String[]{String.valueOf(year)});
    }

    // 🔹 Hàm tổng quát lấy tổng thu nhập/chi tiêu theo khoảng thời gian
    public float getTotalForPeriod(String startDate, String endDate, boolean isIncome) {
        String condition = COLUMN_DATE + " BETWEEN ? AND ? AND " + COLUMN_AMOUNT + (isIncome ? " >= 0" : " < 0");
        return getTotalForCondition(condition, new String[]{startDate, endDate});
    }

    // 🔹 Hàm tổng hợp lấy dữ liệu theo điều kiện
    private float getTotalForCondition(String condition, String[] args) {
        float total = 0;
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_AMOUNT + ") FROM " + TABLE_TRANSACTIONS + " WHERE " + condition, args);
        if (cursor.moveToFirst()) {
            total = cursor.getFloat(0);
        }
        cursor.close();
        return total;
    }

    // 🔹 Tạo đối tượng Transaction từ Cursor
    private Transaction createTransactionFromCursor(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID));
        int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
        String note = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
        boolean isSynced = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_SYNCED)) == 1;

        return new Transaction(id, userId, amount, category, date, note, type, isSynced);
    }

    // 🔹 Lấy thông tin giao dịch theo ID
    public Transaction getTransactionById(int transactionId) {
        Transaction transaction = null;
        Cursor cursor = db.rawQuery("SELECT * FROM transactions WHERE id = ?", new String[]{String.valueOf(transactionId)});

        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id"));
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));
            String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
            String type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
            boolean isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("is_synced")) == 1;

            // Tạo đối tượng Transaction từ dữ liệu lấy được
            transaction = new Transaction(id, userId, amount, category, date, note, type, isSynced);
        }
        cursor.close();
        return transaction;
    }

    public List<Transaction> getTransactionsWithLimit(int limit) {
        List<Transaction> transactionList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM transactions ORDER BY date DESC LIMIT ?", new String[]{String.valueOf(limit)});

        if (cursor.moveToFirst()) {
            do {
                transactionList.add(createTransactionFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    public float getTotalIncomeForDate(String date) {
        float totalIncome = 0;
        Cursor cursor = null;

        try {
            String query = "SELECT SUM(amount) FROM transactions WHERE date = ? AND type = 'income'";
            cursor = db.rawQuery(query, new String[]{date});

            if (cursor.moveToFirst()) {
                totalIncome = cursor.getFloat(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return totalIncome;
    }

}
