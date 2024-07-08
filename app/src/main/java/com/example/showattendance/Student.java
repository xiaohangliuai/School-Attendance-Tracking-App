package com.example.showattendance;


public class Student {
    private String name;
    private String gpsPoints;
    private double distance;
    private String attendance;

    public Student(String name, String gpsPoints, double distance, String attendance) {
        this.name = name;
        this.gpsPoints = gpsPoints;
        this.distance = distance;
        this.attendance = attendance;
    }

    public String getName() {
        return name;
    }

    public String getGpsPoints() {
        return gpsPoints;
    }

    public double getDistance() {
        return distance;
    }

    public String getAttendance() {
        return attendance;
    }
}
