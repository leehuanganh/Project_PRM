package com.example.project.ui_all;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.project.R;
import com.example.project.api.ApiService;
import com.example.project.auth.LoginActivity;
import com.example.project.auth.ChangePasswordActivity;
import com.example.project.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity {

    private EditText edtName, edtEmail, edtPhone;
    private Switch switchSync, switchNotifications;
    private Button btnUpdateProfile, btnChangePassword, btnLogout;
    private SharedPreferences sharedPreferences;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ UI
        switchSync = findViewById(R.id.switchSync);
        switchNotifications = findViewById(R.id.switchNotifications);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnLogout = findViewById(R.id.btnLogout);

        sharedPreferences = getSharedPreferences("user_settings", MODE_PRIVATE);

        // Load trạng thái cài đặt
        loadSettings();

        // Xử lý bật/tắt đồng bộ dữ liệu
        switchSync.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("sync_enabled", isChecked);
            Toast.makeText(this, "Đồng bộ: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
        });

        // Xử lý bật/tắt thông báo
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSetting("notifications_enabled", isChecked);
            Toast.makeText(this, "Thông báo: " + (isChecked ? "Bật" : "Tắt"), Toast.LENGTH_SHORT).show();
        });

        // xử lý cập nhật profile
        btnUpdateProfile.setOnClickListener(this::updateProfile);
        // Xử lý đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> startActivity(new Intent(this, ChangePasswordActivity.class)));

        // Xử lý đăng xuất
        btnLogout.setOnClickListener(this::logoutUser);
    }

    private void loadSettings() {
        boolean isSyncEnabled = sharedPreferences.getBoolean("sync_enabled", false);
        boolean isNotificationsEnabled = sharedPreferences.getBoolean("notifications_enabled", true);

        switchSync.setChecked(isSyncEnabled);
        switchNotifications.setChecked(isNotificationsEnabled);
    }

    public void updateProfile(View view) {
        int userId = sharedPreferences.getInt("userId", -1);
        String name = edtName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<RegisterResponse> call = apiService.updateProfile("update_profile", userId, name, email, phone);
        call.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SettingsActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();

                    // Cập nhật SharedPreferences
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userName", name);
                    editor.putString("userEmail", email);
                    editor.putString("userPhone", phone);
                    editor.apply();
                } else {
                    Toast.makeText(SettingsActivity.this, "Lỗi cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(SettingsActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSetting(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

//    private void updateProfile() {
//        int userId = sharedPreferences.getInt("userId", -1);
//        String name = edtName.getText().toString().trim();
//        String email = edtEmail.getText().toString().trim();
//        String phone = edtPhone.getText().toString().trim();
//
//        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
//            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Call<RegisterResponse> call = apiService.updateProfile("update_profile", userId, name, email, phone);
//        call.enqueue(new Callback<RegisterResponse>() {
//            @Override
//            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
//                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
//                    Toast.makeText(SettingsActivity.this, "Cập nhật thông tin thành công!", Toast.LENGTH_SHORT).show();
//
//                    // Cập nhật SharedPreferences
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("userName", name);
//                    editor.putString("userEmail", email);
//                    editor.putString("userPhone", phone);
//                    editor.apply();
//                } else {
//                    Toast.makeText(SettingsActivity.this, "Lỗi cập nhật thông tin!", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<RegisterResponse> call, Throwable t) {
//                Toast.makeText(SettingsActivity.this, "Lỗi kết nối đến server!", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void logoutUser(View view) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
