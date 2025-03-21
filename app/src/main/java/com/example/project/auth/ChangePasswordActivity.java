package com.example.project.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.project.R;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnSavePassword;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);

        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);

        btnSavePassword.setOnClickListener(v -> changePassword());
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Đổi mật khẩu");
        }

    }

    private void changePassword() {
        String oldPassword = edtOldPassword.getText().toString().trim();
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // Kiểm tra mật khẩu cũ
        String storedPassword = sharedPreferences.getString("userPassword", "");
        if (!oldPassword.equals(storedPassword)) {
            Toast.makeText(this, "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty() || !newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu mới không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu mật khẩu mới
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("userPassword", newPassword);
        editor.apply();

        Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
