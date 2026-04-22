package com.example.smarttracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword;
    private Button btnRegister;
    private ProgressBar progressRegister;
    private SessionManager sessionManager;

    private static final String URL = "http://10.0.2.2/smarttracker/register.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sessionManager = new SessionManager(this);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressRegister = findViewById(R.id.progressRegister);
        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> register());
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        progressRegister.setVisibility(View.VISIBLE);

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL,
                response -> {
                    progressRegister.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    try {
                        JSONObject json = new JSONObject(response);
                        if (json.getBoolean("success")) {
                            sessionManager.saveSession(
                                    json.getInt("userId"),
                                    json.getString("name"),
                                    json.getString("email")
                            );
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, json.getString("message"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(RegisterActivity.this, "Error parsing response",
                                Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressRegister.setVisibility(View.GONE);
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Network error: " + error.toString(),
                            Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(stringRequest);
    }
}
