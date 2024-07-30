package com.example.showattendance;

public class AttendanceRecord {
    private String email;
    private String className;
    private String gpsPoints;

    // Default constructor required for calls to DataSnapshot.getValue(AttendanceRecord.class)
    public AttendanceRecord() {
    }

    public AttendanceRecord(String email, String className, String gpsPoints) {
        this.email = email;
        this.className = className;
        this.gpsPoints = gpsPoints;
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
}
