package com.example.smarttracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.smarttracker.data.Repository;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    //👉 views + helpers for the sign-up screen
    TextInputEditText etName, etEmail, etPassword;
    Button btnRegister;
    TextView tvGoToLogin;
    SessionManager sessionManager;
    Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(RegisterActivity.this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(RegisterActivity.this);
        repository = Repository.get(RegisterActivity.this);

        //✅ here's the view wiring — explicit casts 🧵
        etName = (TextInputEditText) findViewById(R.id.etName);
        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etPassword = (TextInputEditText) findViewById(R.id.etPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        tvGoToLogin = (TextView) findViewById(R.id.tvGoToLogin);

        //👉 register button — lambda flavour 🌮
        btnRegister.setOnClickListener(v -> register());
        /* anonymous version:
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { register(); }
        });
        */

        //✅ "back to login" link — anonymous inner class flavour 🍦
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        /* lambda version:
        tvGoToLogin.setOnClickListener(v -> finish());
        */
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        Repository.AuthResult result = repository.register(name, email, password);
        if (!result.success) {
            Toast.makeText(RegisterActivity.this, result.message, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveSession(result.userId, result.name, result.email);
        startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
        finish();
    }
}
