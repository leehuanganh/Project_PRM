package com.example.project.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.response.LoginResponse;
import com.example.project.ui_all.MainActivity;
import com.example.project.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Kiểm tra xem đã đăng nhập chưa, nếu có thì vào MainActivity luôn
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish(); // Kết thúc LoginActivity
            return;
        }

        setContentView(R.layout.activity_login);

        // Ánh xạ UI
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quản lý chi tiêu");
        }

        // Khởi tạo API Service
        apiService = ApiClient.getClient().create(ApiService.class);

        // 🔹 Xử lý sự kiện đăng nhập
        btnLogin.setOnClickListener(v -> loginUser());

        // 🔹 Chuyển đến màn hình Đăng ký
        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<LoginResponse> call = apiService.loginUser("login", email, password);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // ✅ Lưu trạng thái đăng nhập vào SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.putInt("userId", response.body().getUserId()); // Lưu userId
                    editor.putString("userEmail", email);
                    editor.apply();

                    // ✅ Chuyển đến MainActivity
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish(); // Kết thúc LoginActivity
                } else {
                    Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
                Log.e("LOGIN_ERROR", "Lỗi khi kết nối API: " + t.getMessage());
            }
        });
    }

    private int getUserId() {
        return sharedPreferences.getInt("userId", -1); // Nếu không có, trả về -1
    }

}
