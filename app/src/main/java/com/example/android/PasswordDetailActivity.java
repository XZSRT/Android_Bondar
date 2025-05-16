package com.example.android;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        PasswordModel password = (PasswordModel) getIntent().getSerializableExtra("PASSWORD");

        TextView tvService = findViewById(R.id.tvService);
        TextView tvLogin = findViewById(R.id.tvLogin);
        TextView tvPassword = findViewById(R.id.tvPassword);
        TextView tvNotes = findViewById(R.id.tvNotes);

        tvService.setText(password.getService());
        tvLogin.setText(password.getLogin());
        tvPassword.setText(password.getPassword());
        tvNotes.setText(password.getNotes());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
