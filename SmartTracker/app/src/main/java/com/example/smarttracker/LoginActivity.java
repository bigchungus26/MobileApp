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

public class LoginActivity extends AppCompatActivity {

    //👉 views + helpers for the login screen
    TextInputEditText etEmail, etPassword;
    Button btnLogin;
    TextView tvGoToRegister;
    SessionManager sessionManager;
    Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //👉 if the user is already signed in we skip straight to the main screen
        sessionManager = new SessionManager(LoginActivity.this);
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        EdgeToEdge.enable(LoginActivity.this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = Repository.get(LoginActivity.this);

        //✅ here's the view wiring — explicit casts 🧵
        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etPassword = (TextInputEditText) findViewById(R.id.etPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        tvGoToRegister = (TextView) findViewById(R.id.tvGoToRegister);

        //👉 login button — anonymous inner class flavour
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        /* lambda version:
        btnLogin.setOnClickListener(v -> login());
        */

        //✅ "go to register" link — lambda flavour (mix 'em up 🌮)
        tvGoToRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
        /* anonymous version:
        tvGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
        */
    }

    private void login() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Repository.AuthResult result = repository.login(email, password);
        if (!result.success) {
            Toast.makeText(LoginActivity.this, result.message, Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.saveSession(result.userId, result.name, result.email);
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }
}
