package com.example.smarttracker.data;

public class User {

    public int id;
    public String name;
    public String email;
    public String passwordHash;

    public User() { }

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }
}
