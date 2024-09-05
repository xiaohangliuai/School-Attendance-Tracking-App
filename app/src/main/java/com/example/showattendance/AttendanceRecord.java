package com.example.showattendance;

public class AttendanceRecord {
    private String email;
    private String className;
    private String gpsPoints;
    private String firstName;
    private String lastName;
    private String signInTime;



    // Default constructor required for calls to DataSnapshot.getValue(AttendanceRecord.class)
    public AttendanceRecord() {
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

    public AttendanceRecord(String email, String className, String gpsPoints, String firstName, String lastName, String signInTime) {
        this.email = email;
        this.className = className;
        this.gpsPoints = gpsPoints;
        this.firstName = firstName;
        this.lastName = lastName;
        this.signInTime = signInTime;

    }

    public String getEmail() {
        return email;
    }

    public String getClassName() {
        return className;
    }

    public String getGpsPoints() {
        return gpsPoints;
    }

    public String getSignInTime() {
        return signInTime;
    }

    public void setSignInTime(String signInTime) {
        this.signInTime = signInTime;
    }
}
