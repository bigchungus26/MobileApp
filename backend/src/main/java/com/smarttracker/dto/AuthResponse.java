package com.smarttracker.dto;

public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private Long userId;

    public AuthResponse(String token, String name, String email, Long userId) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.userId = userId;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
