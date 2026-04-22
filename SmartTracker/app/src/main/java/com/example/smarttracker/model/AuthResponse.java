package com.example.smarttracker.model;

public class AuthResponse {
    private String token;
    private String name;
    private String email;
    private long userId;

    public String getToken() { return token; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public long getUserId() { return userId; }
}
