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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project.R;
import com.example.project.adapter.TransactionAdapter;
import com.example.project.api.ApiService;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;
import com.example.project.response.TransactionResponse;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionActivity extends AppCompatActivity {

    private EditText edtAmount, edtNote;
    private Spinner spinnerCategory;
    private TextView tvDate, tvAmount, tvCategory, tvNote;
    private Button btnSave, btnEdit, btnDelete;
    private ImageButton btnPickDate;
    private TransactionDAO transactionDAO;
    private int transactionId = -1;
    private String selectedDate = "";
    private String selectedType = "all"; // "all", "income", "expense"
    private ApiService apiService;
    private List<Transaction> transactionList;
    private TransactionAdapter adapter;

    public static void start(Context context, int transactionId) {
        Intent intent = new Intent(context, TransactionActivity.class);
        intent.putExtra("TRANSACTION_ID", transactionId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);  // Đảm bảo layout đúng!

        // ✅ Khởi tạo các thành phần UI trước khi gán sự kiện click
        edtAmount = findViewById(R.id.edtAmount);
        edtNote = findViewById(R.id.edtNote);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tvDate = findViewById(R.id.tvDate);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnSave = findViewById(R.id.btnSave);

        transactionId = getIntent().getIntExtra("TRANSACTION_ID", -1);
        transactionDAO = new TransactionDAO(this);

        btnPickDate.setOnClickListener(this::showDatePickerDialog);
        btnSave.setOnClickListener(this::saveTransaction);

        Toolbar toolbar = findViewById(R.id.toolbar);


        // Gán tiêu đề tùy chỉnh
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Thêm giao dịch"); // ✅ Tiêu đề của từng màn hình
        }
    }


    public void showDatePickerDialog(View view) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view1, year, month, dayOfMonth) -> {
                    selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                    tvDate.setText(selectedDate);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    public void saveTransaction(View view) {
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

}
