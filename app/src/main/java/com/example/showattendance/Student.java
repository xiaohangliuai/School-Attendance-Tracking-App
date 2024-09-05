package com.example.showattendance;

public class Student {
    private String name;
    private String gpsPoints;
    private double distance;
    private String checkInTime;
    private String attendance;



    public Student(String name, String gpsPoints, double distance, String checkInTime, String attendance) {
        this.name = name;
        this.gpsPoints = gpsPoints;
        this.distance = distance;
        this.checkInTime = checkInTime;
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


    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }
}
