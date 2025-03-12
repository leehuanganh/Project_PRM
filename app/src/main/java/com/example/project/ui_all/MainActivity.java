//package com.example.project.ui_all;
//
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.LinearLayout;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.project.R;
//import com.example.project.adapter.TransactionAdapter;
//import com.example.project.api.ApiClient;
//import com.example.project.api.ApiService;
//import com.example.project.auth.LoginActivity;
//import com.example.project.auth.RegisterActivity;
//import com.example.project.database.DatabaseHelper;
//import com.example.project.model.Transaction;
//import com.example.project.network.NetworkChangeReceiver;
//import com.example.project.response.LoginResponse;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//
//import java.util.List;
//import retrofit2.Call;
//import retrofit2.Callback;
//import retrofit2.Response;
//
//public class MainActivity extends AppCompatActivity {
//
//    private EditText edtEmail, edtPassword;
//    private Button btnLogin, btnRegister;
//    private LinearLayout layoutLogin;
//    private RecyclerView recyclerViewTransactions;
//    private ApiService apiService;
//    private SharedPreferences sharedPreferences;
//    private DatabaseHelper dbHelper;
//    private NetworkChangeReceiver networkChangeReceiver;
//
//    private FloatingActionButton btnAddTransaction;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // ✅ Kiểm tra xem người dùng đã đăng nhập chưa
//        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
//        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
//            startActivity(new Intent(this, LoginActivity.class));
//            finish(); // Đóng MainActivity để tránh quay lại khi ấn "back"
//            return;
//        }
//
//        // ✅ Nếu đã đăng nhập → hiển thị danh sách giao dịch
//        setContentView(R.layout.activity_main);
//        recyclerViewTransactions = findViewById(R.id.recyclerViewTransactions);
//        btnAddTransaction = findViewById(R.id.btnAddTransaction);
//        dbHelper = new DatabaseHelper(this);
//
//        // 🔹 Hiển thị danh sách giao dịch
//        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
//        List<Transaction> transactions = dbHelper.getAllTransactions();
//        TransactionAdapter adapter = new TransactionAdapter(this, transactions);
//        recyclerViewTransactions.setAdapter(adapter);
//
//        // 🔹 Xử lý sự kiện thêm giao dịch
//        btnAddTransaction.setOnClickListener(v -> {
//            startActivity(new Intent(MainActivity.this, TransactionActivity.class));
//        });
//    }
//
//    /** =============================
//     * 🟢 Kiểm tra trạng thái đăng nhập
//     * ============================= */
//    private boolean isUserLoggedIn() {
//        return sharedPreferences.getBoolean("isLoggedIn", false);
//    }
//
//    /** =============================
//     * 🟢 Lưu phiên đăng nhập
//     * ============================= */
//    private void saveUserSession(String email) {
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putBoolean("isLoggedIn", true);
//        editor.putString("userEmail", email);
//        editor.apply();
//    }
//
//    /** =============================
//     * 🟢 Đăng nhập
//     * ============================= */
//    private void loginUser() {
//        String email = edtEmail.getText().toString().trim();
//        String password = edtPassword.getText().toString().trim();
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (!isNetworkAvailable()) {
//            Toast.makeText(this, "Không có kết nối mạng!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Call<LoginResponse> call = apiService.loginUser("login", email, password);
//        call.enqueue(new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    if (response.body().isSuccess()) {
//                        saveUserSession(email);
//                        Toast.makeText(MainActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//                        navigateToDashboard();
//                    } else {
//                        Toast.makeText(MainActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "Lỗi phản hồi từ server!", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Toast.makeText(MainActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
//                Log.e("API_TEST", "Lỗi API: " + t.getMessage());
//            }
//        });
//    }
//
//    /** =============================
//     * 🟢 Chuyển đến màn hình chính (Dashboard)
//     * ============================= */
//    private void navigateToDashboard() {
//        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
//        startActivity(intent);
//        finish();
//    }
//
//    /** =============================
//     * 🟢 Kiểm tra kết nối mạng
//     * ============================= */
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }
//
//    /** =============================
//     * 🟢 Kiểm tra API kết nối (tài khoản test)
//     * ============================= */
//    private void testApiConnection() {
//        Call<LoginResponse> call = apiService.loginUser("login", "test@example.com", "123456");
//        call.enqueue(new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Log.d("API_TEST", "API phản hồi: " + response.body().getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                Log.e("API_TEST", "Lỗi API: " + t.getMessage());
//            }
//        });
//    }
//
//    /** =============================
//     * 🟢 Đăng ký & Hủy đăng ký kiểm tra mạng
//     * ============================= */
//    @Override
//    protected void onResume() {
//        super.onResume();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        networkChangeReceiver = new NetworkChangeReceiver();
//        registerReceiver(networkChangeReceiver, filter);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (networkChangeReceiver != null) {
//            unregisterReceiver(networkChangeReceiver);
//        }
//    }
//}
package com.example.project.ui_all;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.auth.LoginActivity;
import com.example.project.R;
import android.content.SharedPreferences;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Kiểm tra xem người dùng đã đăng nhập chưa
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        if (!sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, LoginActivity.class));
            finish(); // Đóng MainActivity để tránh quay lại khi ấn "back"
            return;
        }

        // ✅ Nếu đã đăng nhập → chuyển đến màn hình thống kê (DashboardActivity)
        startActivity(new Intent(this, DashboardActivity.class));
        finish();
    }
}
