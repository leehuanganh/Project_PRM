package com.example.project.ui_all;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;
import java.util.Calendar;

public class TransactionDetailActivity extends AppCompatActivity {

    private TextView tvAmount, tvCategory, tvDate, tvNote;
    private Button btnEdit, btnDelete;
    private TransactionDAO transactionDAO;
    private int transactionId = -1;
    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail); // Dùng đúng layout

        // Ánh xạ UI
        tvAmount = findViewById(R.id.tvAmount);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvNote = findViewById(R.id.tvNote);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        transactionDAO = new TransactionDAO(this);

        // Nhận ID giao dịch từ Intent
        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);

        if (transactionId != -1) {
            loadTransactionDetails();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Xử lý nút chỉnh sửa & xóa
        btnEdit.setOnClickListener(v -> showEditTransactionDialog());
        btnDelete.setOnClickListener(v -> deleteTransaction());
    }

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

    private void showEditTransactionDialog() {
        // Chuyển đến màn hình chỉnh sửa giao dịch (có thể dùng cùng `TransactionActivity`)
        TransactionActivity.start(this, transactionId);
        finish();
    }

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
