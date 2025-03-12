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
import com.example.project.R;
import com.example.project.api.ApiClient;
import com.example.project.api.ApiService;
import com.example.project.response.RegisterResponse;
import com.example.project.ui_all.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ApiService apiService;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ✅ Ánh xạ UI
        edtEmail = findViewById(R.id.edtRegisterEmail);
        edtPassword = findViewById(R.id.edtRegisterPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // ✅ Khởi tạo API & SharedPreferences
        apiService = ApiClient.getClient().create(ApiService.class);
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);

        // 🔹 Xử lý đăng ký tài khoản
        btnRegister.setOnClickListener(v -> registerUser());

        // 🔹 Chuyển về màn hình đăng nhập
        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    /** ============================
     * 🟢 Xử lý Đăng ký tài khoản
     * ============================ */
    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // ✅ Kiểm tra lỗi đầu vào
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không trùng khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ Gọi API đăng ký
        Call<RegisterResponse> call = apiService.registerUser("register", email, password);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                        // 🔹 Lưu thông tin đăng nhập để không cần đăng nhập lại
                        saveUserSession(email);

                        // 🔹 Chuyển đến màn hình chính
                        navigateToMain();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Lỗi: " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Lỗi phản hồi từ server!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
                Log.e("API_ERROR", "Lỗi API: " + t.getMessage());
            }
        });
    }

    /** ============================
     * 🟢 Lưu trạng thái đăng nhập
     * ============================ */
    private void saveUserSession(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userEmail", email);
        editor.apply();
    }

    /** ============================
     * 🟢 Chuyển đến màn hình chính
     * ============================ */
    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
