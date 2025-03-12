package com.example.project.ui_all;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.adapter.TransactionAdapter;
import com.example.project.database.TransactionDAO;
import com.example.project.model.Transaction;
import java.util.List;

public class TransactionListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewTransactions;
    private TransactionDAO transactionDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Dùng lại layout của MainActivity cũ

        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
        transactionDAO = new TransactionDAO(this);

        // Cấu hình danh sách giao dịch
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        List<Transaction> transactions = transactionDAO.getTransactionsWithLimit(50); // Giới hạn để tối ưu hiệu suất
        TransactionAdapter adapter = new TransactionAdapter(this, transactions);
        recyclerViewTransactions.setAdapter(adapter);
    }
}
