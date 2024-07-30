//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.AdapterView;
//import android.widget.ArrayAdapter;
//import android.widget.Button;
//import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.showattendance.MainActivity;
//import com.example.showattendance.R;
//import com.example.showattendance.Student;
//import com.example.showattendance.StudentAdapter;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QueryDocumentSnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class TeacherActivity extends AppCompatActivity {
//
//    private static final String TAG = "TeacherActivity";
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
//
//    private Spinner spinnerClasses;
//    private Button buttonShowStudents;
//    private Button buttonSignOut;
//    private RecyclerView recyclerView;
//    private TextView teacherGps;
//
//    private FusedLocationProviderClient fusedLocationClient;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//
//    private List<Student> studentList;
//    private StudentAdapter studentAdapter;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_teacher);
//
//        Log.d(TAG, "onCreate: Initializing views");
//
//        spinnerClasses = findViewById(R.id.spinnerClasses);
//        buttonShowStudents = findViewById(R.id.buttonShowStudents);
//        recyclerView = findViewById(R.id.recyclerView);
//        teacherGps = findViewById(R.id.textViewTeacherGps);
//        buttonSignOut = findViewById(R.id.buttonSignOut);
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        studentList = new ArrayList<>();
//        studentAdapter = new StudentAdapter(studentList);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setAdapter(studentAdapter);
//
//        loadCourses();
//
//        spinnerClasses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String selectedClass = (String) parent.getItemAtPosition(position);
//                fetchStudentData(selectedClass);
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // Handle case when no item is selected
//            }
//        });
//
//        buttonShowStudents.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//            } else {
//                getInformation();
//            }
//        });
//
//        buttonSignOut.setOnClickListener(v -> {
//            Intent intent = new Intent(TeacherActivity.this, MainActivity.class);
//            startActivity(intent);
//        });
//    }
//
//    private void loadCourses() {
//        db.collection("Courses").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                List<String> courses = new ArrayList<>();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String courseName = document.getString("className");
//                    if (courseName != null) {
//                        courses.add(courseName);
//                    }
//                }
//                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                        android.R.layout.simple_spinner_item, courses);
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                spinnerClasses.setAdapter(adapter);
//            } else {
//                Toast.makeText(TeacherActivity.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void getInformation() {
//        Log.d(TAG, "Getting information...");
//        try {
//            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//                if (location != null) {
//                    double teacherLatitude = location.getLatitude();
//                    double teacherLongitude = location.getLongitude();
//                    teacherGps.setText("Latitude: " + teacherLatitude + ", Longitude: " + teacherLongitude);
//                    Log.d(TAG, "Teacher location: " + teacherLatitude + ", " + teacherLongitude);
//                    fetchStudentData(spinnerClasses.getSelectedItem().toString());
//                } else {
//                    Log.e(TAG, "Location is null");
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void fetchStudentData(String selectedClass) {
//        Log.d(TAG, "Fetching student data...");
//        db.collection("Attendance").whereEqualTo("className", selectedClass).get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        studentList.clear();
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String email = document.getString("email");
//                            String gpsPoints = document.getString("gpsPoints");
//                            if (email != null && gpsPoints != null) {
//                                String[] gpsPointsArray = gpsPoints.split(", ");
//                                double studentLatitude = Double.parseDouble(gpsPointsArray[0]);
//                                double studentLongitude = Double.parseDouble(gpsPointsArray[1]);
//
//                                float[] results = new float[1];
//                                Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
//                                double distance = results[0];
//                                distance = Math.round(distance * 100.0) / 100.0;
//                                String attendance = distance < 10 ? "Present" : "Absent";
//
//                                studentList.add(new Student(email, gpsPoints, distance, attendance));
//                                Log.d(TAG, "Student added: " + email + ", " + gpsPoints + ", " + distance + ", " + attendance);
//                            } else {
//                                Log.d(TAG, "Student " + email + " has no GPS points");
//                            }
//                        }
//                        studentAdapter.notifyDataSetChanged();
//                    } else {
//                        Log.e(TAG, "Error getting documents: ", task.getException());
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
//
//






package com.example.showattendance;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
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


public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Button buttonShowStudents;
    private  Button buttonSignOut;
    private RecyclerView recyclerView;
    private TextView teacherGps;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<Student> studentList;
    private StudentAdapter studentAdapter;
    private List<String> classList;
    private Spinner spinnerClasses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        Log.d(TAG, "onCreate: Initializing views");

        buttonShowStudents = findViewById(R.id.buttonShowStudents);
        recyclerView = findViewById(R.id.recyclerView);
        teacherGps = findViewById(R.id.textViewTeacherGps);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        spinnerClasses = findViewById(R.id.spinnerClasses);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(studentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);

        classList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasses.setAdapter(adapter);

        fetchClasses(adapter);

        buttonShowStudents.setOnClickListener(v -> {
            Log.d(TAG, "Show Students button clicked");
            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getInformation();
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }

    private void fetchClasses(ArrayAdapter<String> adapter) {
        db.collection("Classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                classList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String className = document.getString("name");
                    classList.add(className);
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error fetching classes: ", task.getException());
//                Toast.makeText(StudentActivity.this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
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
                                distance = Math.round(distance * 100.0) / 100.0;
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
//
//
