package com.example.project.ui_all;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;

public class TransactionDetailActivity extends AppCompatActivity {

    private TextView tvAmount, tvCategory, tvDate, tvNote;
    private Button btnEdit, btnDelete;
    private TransactionDAO transactionDAO;
    private int transactionId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);


        // Gán tiêu đề tùy chỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Chi tiết giao dịch"); // ✅ Tiêu đề của từng màn hình
        }

        // Ánh xạ UI
        tvAmount = findViewById(R.id.tvAmount);
        tvCategory = findViewById(R.id.tvCategory);
        tvDate = findViewById(R.id.tvDate);
        tvNote = findViewById(R.id.tvNote);
        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        transactionDAO = new TransactionDAO(this);
        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);


        if (transactionId != -1) {
            loadTransactionDetails();
        } else {
            Toast.makeText(this, "Lỗi: Không tìm thấy giao dịch", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Chỉnh sửa giao dịch
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionActivity.class);
            intent.putExtra("TRANSACTION_ID", transactionId);
            startActivity(intent);
        });

        // Xóa giao dịch
        btnDelete.setOnClickListener(v -> {
            boolean isDeleted = transactionDAO.deleteTransaction(transactionId);
            if (isDeleted) {
                Toast.makeText(this, "Xóa giao dịch thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi xóa!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTransactionDetails() {
        Transaction transaction = transactionDAO.getTransactionById(transactionId);
        if (transaction != null) {
            tvAmount.setText("Số tiền: " + String.format("%,.0f VND", transaction.getAmount()));
            tvCategory.setText("Danh mục: " + transaction.getCategory());
            tvDate.setText("Ngày: " + transaction.getDate());
            tvNote.setText("Ghi chú: " + transaction.getNote());
        }
    }
}
