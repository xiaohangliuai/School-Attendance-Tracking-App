package com.example.showattendance;

public class User {
    private String email;
    private String role;
    private String gpsPoints;

    public User() {
    }

    public User(String email, String role, String gpsPoints) {
        this.email = email;
        this.role = role;
        this.gpsPoints = gpsPoints;
    }

    public String getEmail() {
        return email;
    }

    public void setName(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getGpsPoints() {
        return gpsPoints;
    }

    public void setGpsPoints(String gpsPoints) {
        this.gpsPoints = gpsPoints;
    }
}
