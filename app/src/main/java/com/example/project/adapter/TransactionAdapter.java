package com.example.project.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project.R;
import com.example.project.model.Transaction;
import com.example.project.ui_all.TransactionDetailActivity;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactionList;

    public TransactionAdapter(Context context, List<Transaction> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.tvCategory.setText(transaction.getCategory());
        holder.tvDate.setText(transaction.getDate());
        holder.tvAmount.setText(String.format("%,.0f VND", transaction.getAmount()));

        // 🟢 Đổi màu số tiền: Đỏ nếu chi tiêu, Xanh nếu thu nhập
        int color = transaction.getType().equals("expense") ? R.color.red : R.color.green;
        holder.tvAmount.setTextColor(ContextCompat.getColor(context, color));

        // 🟢 Set icon danh mục
        holder.imgCategory.setImageResource(getCategoryIcon(transaction.getCategory()));

        // 🟢 Sự kiện click vào item để mở TransactionDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TransactionDetailActivity.class);
            intent.putExtra("TRANSACTION_ID", transaction.getId());
            intent.putExtra("AMOUNT", transaction.getAmount());
            intent.putExtra("CATEGORY", transaction.getCategory());
            intent.putExtra("DATE", transaction.getDate());
            intent.putExtra("NOTE", transaction.getNote());
            intent.putExtra("TYPE", transaction.getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    /** ==============================
     * 🔹 ViewHolder Class
     * ============================== */
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvDate, tvAmount;
        ImageView imgCategory; // 🟢 Icon danh mục

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            imgCategory = itemView.findViewById(R.id.imgCategory);
        }
    }

    /** ==============================
     * 🔹 Hàm cập nhật dữ liệu danh sách
     * ============================== */
    public void updateData(List<Transaction> newTransactions) {
        transactionList.clear();
        transactionList.addAll(newTransactions);
        notifyDataSetChanged();
    }

    /** ==============================
     * 🔹 Hàm lấy icon theo danh mục
     * ============================== */
    private int getCategoryIcon(String category) {
        switch (category) {
            case "Ăn uống":
                return R.drawable.ic_food;
            case "Mua sắm":
                return R.drawable.ic_shopping;
            case "Di chuyển":
                return R.drawable.ic_transport;
            case "Giải trí":
                return R.drawable.ic_entertainment;
            case "Sức khỏe":
                return R.drawable.ic_health;
            case "Lương":
                return R.drawable.ic_salary;
            case "Khác":
                return R.drawable.ic_category;
            default:
                return R.drawable.ic_category; // 🟢 Default icon
        }
    }
}
