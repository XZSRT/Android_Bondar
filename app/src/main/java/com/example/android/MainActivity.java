package com.example.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ListView lvPasswords;
    private TextView tvEmpty;
    private PasswordAdapter adapter;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        lvPasswords = findViewById(R.id.lvPasswords);
        tvEmpty = findViewById(R.id.tvEmpty);

        FloatingActionButton fab = findViewById(R.id.fabAddPassword);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddPasswordActivity.class);
            intent.putExtra("USERNAME", username);
            startActivityForResult(intent, 1);
            overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_down);
        });

        lvPasswords.setOnItemClickListener((parent, view, position, id) -> {
            PasswordModel password = (PasswordModel) parent.getItemAtPosition(position);
            Intent intent = new Intent(MainActivity.this, PasswordDetailActivity.class);
            intent.putExtra("PASSWORD", password);
            startActivity(intent);
        });

        loadPasswords();
    }

    private void loadPasswords() {
        int userId = dbHelper.getUserId(username);
        List<PasswordModel> passwordList = dbHelper.getAllPasswords(userId);

        if (passwordList.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            lvPasswords.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            lvPasswords.setVisibility(View.VISIBLE);
            adapter = new PasswordAdapter(this, passwordList);
            lvPasswords.setAdapter(adapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadPasswords(); // Обновляем список паролей
        }
    }
}