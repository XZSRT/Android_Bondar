package com.example.android;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddPasswordActivity extends AppCompatActivity {
    private EditText etService, etLogin, etPassword, etNotes;
    private CheckBox cbShowPassword;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        initViews();
        setupListeners();
    }

    private void initViews() {
        etService = findViewById(R.id.etService);
        etLogin = findViewById(R.id.etLogin);
        etPassword = findViewById(R.id.etPassword);
        etNotes = findViewById(R.id.etNotes);
        cbShowPassword = findViewById(R.id.cbShowPassword);
        btnSave = findViewById(R.id.btnSave);
    }

    private void setupListeners() {
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                        android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        btnSave.setOnClickListener(v -> savePassword());
    }

    private void savePassword() {
        String service = etService.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (service.isEmpty() || login.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = dbHelper.getUserId(username);
        if (dbHelper.addPassword(userId, service, login, password, notes)) {
            Toast.makeText(this, "Пароль сохранен", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        } else {
            Toast.makeText(this, "Ошибка сохранения", Toast.LENGTH_SHORT).show();
        }
    }
}