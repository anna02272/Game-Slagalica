package com.example.slagalica.login_registration;


public class User {
    private String username;
    private int tokens;
    private int stars;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public User(String username, int tokens, int stars) {
        this.username = username;
        this.tokens = tokens;
        this.stars = stars;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }
    public int getStars() {
        return stars;
    }
    public void setTStars(int stars) {
        this.stars = stars;
    }
}

