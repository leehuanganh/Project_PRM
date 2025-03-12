package com.example.project.ui_all;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;

public class AddTransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote;
    private Spinner spinnerCategory;
    private Button btnSave;
    private TransactionDAO transactionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSave = findViewById(R.id.btnSave);

        transactionDAO = new TransactionDAO(this);

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String amountStr = edtAmount.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        String note = edtNote.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String date = "2025-03-08"; // Tạm thời hardcode, sau này sẽ dùng DatePicker
        int userId = 1; // Tạm thời hardcode, sau này lấy từ SharedPreferences
        String type = "expense"; // Mặc định là chi tiêu, có thể thêm Spinner để chọn

        // ✅ Tạo Transaction với đầy đủ 8 tham số
        Transaction transaction = new Transaction(0, userId, amount, category, date, note, type, false);

        boolean isInserted = transactionDAO.addTransaction(transaction);

        if (isInserted) {
            Toast.makeText(this, "Thêm giao dịch thành công!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi thêm giao dịch!", Toast.LENGTH_SHORT).show();
        }
    }

}
