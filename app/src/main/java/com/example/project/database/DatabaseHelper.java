package com.example.project.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.model.Transaction;
import com.example.project.response.LoginResponse;
import com.example.project.response.RegisterResponse;
import com.example.project.response.TransactionResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_manager.db";
    private static final int DATABASE_VERSION = 2;

    // Bảng người dùng
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    // Bảng giao dịch
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANSACTION_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_IS_SYNCED = "is_synced"; // 0: Chưa đồng bộ, 1: Đã đồng bộ

    private ApiService apiService;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo bảng người dùng
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Tạo bảng giao dịch
        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_AMOUNT + " REAL, " +
                COLUMN_CATEGORY + " TEXT, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_NOTE + " TEXT, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_IS_SYNCED + " INTEGER DEFAULT 0)";
        db.execSQL(createTransactionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }

    // 🔹 Thêm giao dịch vào SQLite
    public void addTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, transaction.getUserId());
        values.put(COLUMN_AMOUNT, transaction.getAmount());
        values.put(COLUMN_CATEGORY, transaction.getCategory());
        values.put(COLUMN_DATE, transaction.getDate());
        values.put(COLUMN_NOTE, transaction.getNote());
        values.put(COLUMN_TYPE, transaction.getType());
        values.put(COLUMN_IS_SYNCED, 0); // Mặc định chưa đồng bộ

        db.insert(TABLE_TRANSACTIONS, null, values);
        db.close();
    }

    // 🔹 Lấy danh sách tất cả giao dịch từ SQLite
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " ORDER BY date DESC", null);

        if (cursor.moveToFirst()) {
            do {
                transactionList.add(createTransactionFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    // 🔹 Lấy danh sách giao dịch chưa đồng bộ
    public List<Transaction> getUnsyncedTransactions() {
        List<Transaction> transactionList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_IS_SYNCED + "=0", null);

        if (cursor.moveToFirst()) {
            do {
                transactionList.add(createTransactionFromCursor(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return transactionList;
    }

    // 🔹 Hàm tạo đối tượng Transaction từ Cursor
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

    // 🔹 Đồng bộ giao dịch với MySQL
    public void syncTransactions() {
        List<Transaction> unsyncedTransactions = getUnsyncedTransactions();

        for (Transaction transaction : unsyncedTransactions) {
            Call<TransactionResponse> call = apiService.addTransaction(
                    "add_transaction",
                    transaction.getUserId(),
                    transaction.getCategory(),
                    transaction.getAmount(),
                    transaction.getDate(),
                    transaction.getNote(),
                    transaction.getType()
            );

            call.enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        updateSyncStatus(transaction.getId(), true);
                    }
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Log.e("SyncError", "Lỗi đồng bộ: " + t.getMessage());
                }
            });
        }
    }

    // 🔹 Cập nhật trạng thái đồng bộ
    public void updateSyncStatus(int transactionId, boolean isSynced) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_SYNCED, isSynced ? 1 : 0);

        db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_ID + "=?", new String[]{String.valueOf(transactionId)});
        db.close();
    }

    // 🔹 Đăng ký người dùng vào SQLite và đồng bộ với MySQL
    public boolean registerUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        if (result != -1) {
            syncUserToMySQL(email, password);
            return true;
        } else {
            return false;
        }
    }

    // 🔹 Đồng bộ tài khoản lên MySQL
    private void syncUserToMySQL(String email, String password) {
        Call<RegisterResponse> call = apiService.registerUser("register", email, password);

        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                Log.d("SYNC_USER", "Kết quả: " + response.body().getMessage());
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Log.e("SYNC_USER", "Lỗi: " + t.getMessage());
            }
        });
    }

    public void checkUser(String email, String password, final CheckUserCallback callback) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Kiểm tra trong SQLite trước
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?", new String[]{email});

        if (cursor.moveToFirst()) {
            String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
            cursor.close();
            db.close();

            // Nếu mật khẩu đúng, xác thực thành công
            if (password.equals(storedPassword)) {
                callback.onResult(true);
                return;
            }
        }
        cursor.close();
        db.close();

        // Nếu không tìm thấy trong SQLite, kiểm tra MySQL
        Call<LoginResponse> call = apiService.loginUser("login", email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean success = response.body().isSuccess();
                    if (success) {
                        // Nếu đăng nhập thành công trên MySQL, lưu vào SQLite để lần sau dùng offline
                        saveUserToSQLite(email, password);
                    }
                    callback.onResult(success);
                } else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", "Lỗi API đăng nhập: " + t.getMessage());
                callback.onResult(false);
            }
        });
    }

    // 🔹 Hàm lưu tài khoản vào SQLite sau khi xác thực từ MySQL
    private void saveUserToSQLite(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    // 🔹 Interface xử lý callback cho đăng nhập
    public interface CheckUserCallback {
        void onResult(boolean success);
    }


}
