package com.example.showattendance;

public class User {
    private String email;
    private String role;
    private String gpsPoints;
    private String firstName;
    private String lastName;

//    define a no-argument constructor
    public User() {
    }

    public User(String email, String role, String gpsPoints, String firstName, String lastName) {
        this.email = email;
        this.role = role;
        this.gpsPoints = gpsPoints;
        this.firstName = firstName;
        this.lastName = lastName;
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


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
