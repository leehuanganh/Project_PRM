package com.example.project.ui_all;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;
import java.util.Calendar;

public class TransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote;
    private Spinner spinnerCategory;
    private TextView tvDate, tvAmount, tvCategory, tvNote;
    private Button btnSave, btnEdit, btnDelete;
    private ImageButton btnPickDate;
    private TransactionDAO transactionDAO;
    private int transactionId = -1;
    private String selectedDate = "";

    public static void start(Context context, int transactionId) {
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        transactionDAO = new TransactionDAO(this);

        if (transactionId == -1) {
            // Chế độ thêm giao dịch
            setContentView(R.layout.activity_add_transaction);
            setupAddTransactionUI();
        } else {
            // Chế độ xem/sửa/xóa giao dịch
            setContentView(R.layout.activity_transaction_detail);
            setupTransactionDetailUI();
            loadTransactionDetails();
        }
    }

    /** =================================
     * 1️⃣ Giao diện thêm giao dịch
     * ================================= */
    private void setupAddTransactionUI() {
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);
        btnPickDate = findViewById(R.id.btnPickDate);
        tvDate = findViewById(R.id.tvDate);

        btnPickDate.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> saveTransaction());
    }

    /** =================================
     * 2️⃣ Giao diện chi tiết giao dịch (Xem/Sửa/Xóa)
     * ================================= */
    private void setupTransactionDetailUI() {
        tvAmount = findViewById(R.id.tvAmount);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvNote = findViewById(R.id.tvNote);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> openEditTransaction());
        btnDelete.setOnClickListener(v -> deleteTransaction());
    }

    /** =================================
     * 3️⃣ Hiển thị chi tiết giao dịch
     * ================================= */
    private void loadTransactionDetails() {
        Transaction transaction = transactionDAO.getTransactionById(transactionId);
        if (transaction != null) {
            tvAmount.setText("Số tiền: " + String.format("%,.0f VND", transaction.getAmount()));
            tvCategory.setText("Danh mục: " + transaction.getCategory());
            tvDate.setText("Ngày: " + transaction.getDate());
            tvNote.setText("Ghi chú: " + transaction.getNote());
            selectedDate = transaction.getDate();
        }
    }

    /** =================================
     * 4️⃣ Chọn ngày từ DatePicker
     * ================================= */
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    tvDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /** =================================
     * 5️⃣ Lưu giao dịch mới vào Database
     * ================================= */
    private void saveTransaction() {
        String amountStr = edtAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String note = edtNote.getText().toString().trim();

        if (amountStr.isEmpty() || selectedDate.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        Transaction transaction = new Transaction(0, amount, category, selectedDate, note);
        boolean isInserted = transactionDAO.addTransaction(transaction);

        if (isInserted) {
            Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi thêm giao dịch!", Toast.LENGTH_SHORT).show();
        }
    }

    /** =================================
     * 6️⃣ Chuyển sang màn hình chỉnh sửa giao dịch
     * ================================= */
    private void openEditTransaction() {
        Intent intent = new Intent(this, TransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        startActivity(intent);
        finish();
    }

    /** =================================
     * 7️⃣ Xóa giao dịch khỏi Database
     * ================================= */
    private void deleteTransaction() {
        boolean isDeleted = transactionDAO.deleteTransaction(transactionId);
        if (isDeleted) {
            Toast.makeText(this, "Xóa giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
        }
    }
}
