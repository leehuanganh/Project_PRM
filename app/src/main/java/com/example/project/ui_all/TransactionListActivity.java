package com.example.project.ui_all;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.adapter.TransactionAdapter;
import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;
import com.example.project.response.TransactionResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionAdapter transactionAdapter;
    private TransactionDAO transactionDAO;
    private List<Transaction> transactionList;
    private boolean isLoading = false;
    private int currentPage = 1;
    private final int PAGE_SIZE = 20;

    private Button btnFilterDate, btnApplyFilter;
    private Spinner spinnerTransactionType;
    private String selectedDate = "";
    private String selectedType = "all"; // "all", "income", "expense"

    private ApiService apiService;
    private int userId; // Lưu ID của user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnApplyFilter = findViewById(R.id.btnApplyFilter);
        spinnerTransactionType = findViewById(R.id.spinnerTransactionType);

        transactionDAO = new TransactionDAO(this);
        apiService = ApiClient.getClient().create(ApiService.class);
        transactionList = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(this, transactionList);
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionAdapter);

        userId = getUserId(); // Lấy userId từ SharedPreferences

        setupRecyclerViewScrollListener();
        setupFilterActions();

        // Load giao dịch ban đầu
        loadTransactions(currentPage);
    }

    /** ============================================
     * 🔹 Lấy userId từ SharedPreferences
     * ============================================ */
    private int getUserId() {
        SharedPreferences sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        return sharedPreferences.getInt("userId", -1);
    }

    /** ============================================
     * 🔹 Xử lý sự kiện chọn bộ lọc (Ngày & Loại giao dịch)
     * ============================================ */
    private void setupFilterActions() {
        btnFilterDate.setOnClickListener(v -> showDatePickerDialog());

        btnApplyFilter.setOnClickListener(v -> {
            selectedType = getSelectedTransactionType();
            currentPage = 1;
            transactionList.clear();
            loadTransactions(currentPage);
        });
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(year, month, dayOfMonth);
                    selectedDate = sdf.format(selectedCalendar.getTime());
                    Toast.makeText(TransactionListActivity.this, "Lọc theo ngày: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String getSelectedTransactionType() {
        int position = spinnerTransactionType.getSelectedItemPosition();
        return (position == 1) ? "income" : (position == 2) ? "expense" : "all";
    }

    /** ============================================
     * 🔹 Phân trang: Tải thêm dữ liệu khi cuộn xuống
     * ============================================ */
    private void setupRecyclerViewScrollListener() {
        recyclerViewTransactions.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading) {
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
                    if (lastVisibleItemPosition == transactionList.size() - 1) {
                        loadTransactions(currentPage);
                    }
                }
            }
        });
    }

    /** ============================================
     * 🔹 Tải giao dịch từ database & API (phân trang)
     * ============================================ */
    private void loadTransactions(int page) {
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không xác định được userId!", Toast.LENGTH_SHORT).show();
            return;
        }

        isLoading = true;
        int offset = (page - 1) * PAGE_SIZE;

        // ✅ Lọc dữ liệu trong SQLite trước
        List<Transaction> localTransactions = transactionDAO.getTransactionsWithLimit(PAGE_SIZE, offset, selectedDate, selectedType);
        if (!localTransactions.isEmpty()) {
            transactionList.addAll(localTransactions);
            transactionAdapter.notifyDataSetChanged();
            currentPage++;
            isLoading = false;
        } else {
            // ✅ Nếu không có dữ liệu trong SQLite, lấy từ API
            Call<TransactionResponse> call = apiService.getTransactions("get_transactions", userId, page, PAGE_SIZE, selectedDate, selectedType);
            call.enqueue(new Callback<TransactionResponse>() {
                @Override
                public void onResponse(Call<TransactionResponse> call, Response<TransactionResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        transactionList.addAll(response.body().getTransactions());
                        transactionAdapter.notifyDataSetChanged();
                        currentPage++;
                    } else {
                        Toast.makeText(TransactionListActivity.this, "Không có giao dịch nào!", Toast.LENGTH_SHORT).show();
                    }
                    isLoading = false;
                }

                @Override
                public void onFailure(Call<TransactionResponse> call, Throwable t) {
                    Toast.makeText(TransactionListActivity.this, "Lỗi kết nối!", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Lỗi tải giao dịch: " + t.getMessage());
                    isLoading = false;
                }
            });
        }
    }
}
