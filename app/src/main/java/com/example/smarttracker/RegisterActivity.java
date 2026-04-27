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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.smarttracker.util.ApiConfig;
import com.example.smarttracker.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etPassword;
    Button btnRegister;
    TextView tvGoToLogin;
    SessionManager sessionManager;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(RegisterActivity.this);
        setContentView(R.layout.activity_register);
        //pad the layout around the system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        sessionManager = new SessionManager(RegisterActivity.this);
        queue = Volley.newRequestQueue(RegisterActivity.this);

        //link form fields to the layout
        etName = (TextInputEditText) findViewById(R.id.etName);
        etEmail = (TextInputEditText) findViewById(R.id.etEmail);
        etPassword = (TextInputEditText) findViewById(R.id.etPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        tvGoToLogin = (TextView) findViewById(R.id.tvGoToLogin);

        //register button kicks off the create account request
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { register(); }
        });

        //back link just closes this screen and returns to login
        tvGoToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //validate the form then send it to the register endpoint
    private void register() {
        final String name = etName.getText().toString().trim();
        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        //all three fields are required
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        //check the email matches the standard pattern
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            return;
        }
        //enforce a minimum password length
        if (password.length() < 6) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest request = new StringRequest(Request.Method.POST, ApiConfig.REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            //read the success flag from the response
                            JSONObject json = new JSONObject(response);
                            if (!json.optBoolean("success", false)) {
                                Toast.makeText(RegisterActivity.this,
                                        json.optString("message", "Registration failed"),
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //grab the new user info from the response
                            int userId = json.getInt("userId");
                            String userName = json.getString("name");
                            String userEmail = json.getString("email");

                            //auto log the user in and clear the back stack
                            sessionManager.saveSession(userId, userName, userEmail);
                            startActivity(new Intent(RegisterActivity.this, MainActivity.class)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(RegisterActivity.this,
                                    "Bad response from server", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                }) {
            //POST body sent to register.php
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        queue.add(request);
    }
}
