package com.example.showattendance;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
//
//public class TeacherActivity extends AppCompatActivity {
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
//
//    private Button buttonShowStudents;
//    private TextView teacherGps;
//    private RecyclerView recyclerView;
//    private StudentAdapter studentAdapter;
//    private List<Student> studentList = new ArrayList<>();
//    private FusedLocationProviderClient fusedLocationClient;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_teacher);
//
//        buttonShowStudents = findViewById(R.id.buttonShowStudents);
//        recyclerView = findViewById(R.id.recyclerView);
//        teacherGps = findViewById(R.id.textViewTeacherGps);
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        studentAdapter = new StudentAdapter(studentList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(studentAdapter);
//
//        studentList = new ArrayList<>();
//
//        buttonShowStudents.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//            } else {
//                getInformation();
//            }
//        });
//    }
//
//    private void getInformation() {
//        try {
//            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//                if (location != null) {
//                    double teacherLatitude = location.getLatitude();
//                    double teacherLongitude = location.getLongitude();
//                    teacherGps.setText("Latitude: " + teacherLatitude + ", Longitude: " + teacherLongitude);
//                    fetchStudentData(teacherLatitude, teacherLongitude);
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void fetchStudentData(double teacherLatitude, double teacherLongitude) {
//        db.collection("Users").whereEqualTo("role", "Student").get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        studentList.clear();
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            User user = document.toObject(User.class);
//                            String[] gpsPoints = user.getGpsPoints().split(", ");
//                            double studentLatitude = Double.parseDouble(gpsPoints[0]);
//                            double studentLongitude = Double.parseDouble(gpsPoints[1]);
//
//                            float[] results = new float[1];
//                            Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
//                            double distance = results[0];
//                            String attendance = distance < 10 ? "Present" : "Absent";
//
//                            studentList.add(new Student(user.getEmail(), user.getGpsPoints(), distance, attendance));
//                        }
//                        studentAdapter.notifyDataSetChanged();
//                    } else {
//                        Toast.makeText(TeacherActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getInformation();
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}











import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Button buttonShowStudents;
    private RecyclerView recyclerView;
    private TextView teacherGps;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Student> studentList;
    private StudentAdapter studentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        Log.d(TAG, "onCreate: Initializing views");

        buttonShowStudents = findViewById(R.id.buttonShowStudents);
        recyclerView = findViewById(R.id.recyclerView);
        teacherGps = findViewById(R.id.textViewTeacherGps);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);

        buttonShowStudents.setOnClickListener(v -> {
            Log.d(TAG, "Show Students button clicked");
            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getInformation();
            }
        });
    }

    private void getInformation() {
        Log.d(TAG, "Getting information...");
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double teacherLatitude = location.getLatitude();
                    double teacherLongitude = location.getLongitude();
                    teacherGps.setText("Latitude: " + teacherLatitude + ", Longitude: " + teacherLongitude);
                    Log.d(TAG, "Teacher location: " + teacherLatitude + ", " + teacherLongitude);
                    fetchStudentData(teacherLatitude, teacherLongitude);
                } else {
                    Log.e(TAG, "Location is null");
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void fetchStudentData(double teacherLatitude, double teacherLongitude) {
        Log.d(TAG, "Fetching student data...");
        db.collection("Users").whereEqualTo("role", "Student").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            String gpsPoints = user.getGpsPoints();
                            if (gpsPoints != null && !gpsPoints.isEmpty()) {
                                String[] gpsPointsArray = gpsPoints.split(", ");
                                double studentLatitude = Double.parseDouble(gpsPointsArray[0]);
                                double studentLongitude = Double.parseDouble(gpsPointsArray[1]);

                                float[] results = new float[1];
                                Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
                                double distance = results[0];
                                String attendance = distance < 10 ? "Present" : "Absent";

                                studentList.add(new Student(user.getEmail(), gpsPoints, distance, attendance));
                                Log.d(TAG, "Student added: " + user.getEmail() + ", " + gpsPoints + ", " + distance + ", " + attendance);
                            } else {
                                Log.d(TAG, "Student " + user.getEmail() + " has no GPS points");
                            }
                        }
                        studentAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(TeacherActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getInformation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}


