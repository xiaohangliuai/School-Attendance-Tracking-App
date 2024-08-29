//package com.example.showattendance;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
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
//    private List<String> classList;
//    private Spinner spinnerClasses;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_teacher);
//
//        Log.d(TAG, "onCreate: Initializing views");
//
//        buttonShowStudents = findViewById(R.id.buttonShowStudents);
//        recyclerView = findViewById(R.id.recyclerView);
//        teacherGps = findViewById(R.id.textViewTeacherGps);
//        buttonSignOut = findViewById(R.id.buttonSignOut);
//        spinnerClasses = findViewById(R.id.spinnerClasses);
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
//        classList = new ArrayList<>();
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerClasses.setAdapter(adapter);
//
//        fetchClasses(adapter);
//
//        buttonShowStudents.setOnClickListener(v -> {
//            Log.d(TAG, "Show Students button clicked");
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
//    private void fetchClasses(ArrayAdapter<String> adapter) {
//        db.collection("Classes").get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                classList.clear();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String className = document.getString("name");
//                    classList.add(className);
//                }
//                adapter.notifyDataSetChanged();
//            } else {
//                Log.e(TAG, "Error fetching classes: ", task.getException());
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
//                    fetchStudentData(teacherLatitude, teacherLongitude);
//                } else {
//                    Log.e(TAG, "Location is null");
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void fetchStudentData(double teacherLatitude, double teacherLongitude) {
//        Log.d(TAG, "Fetching student data...");
//        String selectedClass = (String) spinnerClasses.getSelectedItem();
//        Log.d(TAG, "Selected class: " + selectedClass);
//
//        db.collection("Attendance").whereEqualTo("className", selectedClass).get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        studentList.clear();
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String userEmail = document.getString("email");
//                            String firstName = document.getString("firstName");
//                            String lastName = document.getString("lastName");
//                            String gpsPoints = document.getString("gpsPoints");
//
//                            if (gpsPoints != null && !gpsPoints.isEmpty()) {
//                                String[] gpsPointsArray = gpsPoints.split(", ");
//                                double studentLatitude = Double.parseDouble(gpsPointsArray[0]);
//                                double studentLongitude = Double.parseDouble(gpsPointsArray[1]);
//
//                                float[] results = new float[1];
//                                Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
//                                double distance = Math.round(results[0] * 100.0) / 100.0;
//                                String attendance = distance < 10 ? "Present" : "Absent";
//
//                                String fullName = firstName + " " + lastName;
//                                studentList.add(new Student(fullName, gpsPoints, distance, attendance));
//                                Log.d(TAG, "Student added: " + fullName + ", " + gpsPoints + ", " + distance + ", " + attendance);
//                            } else {
//                                Log.d(TAG, "Student " + userEmail + " has no GPS points");
//                            }
//                        }
//                        studentAdapter.notifyDataSetChanged();
//                    } else {
//                        Log.e(TAG, "Error getting attendance records: ", task.getException());
//                        Toast.makeText(TeacherActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
//                    }
//                });
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends AppCompatActivity {

    private static final String TAG = "TeacherActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Button buttonShowStudents;
    private Button buttonSignOut;
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

        // Check if the teacher has selected courses; if not, prompt for selection
        checkAndPromptCourseSelection(adapter);

        buttonShowStudents.setOnClickListener(v -> {
            Log.d(TAG, "Show Students button clicked");
            if (ContextCompat.checkSelfPermission(TeacherActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(TeacherActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getInformation();
            }
        });

        buttonSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(TeacherActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void checkAndPromptCourseSelection(ArrayAdapter<String> adapter) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> teacherClasses = (List<String>) document.get("Classes");
                                if (teacherClasses != null && !teacherClasses.isEmpty()) {
                                    classList.addAll(teacherClasses);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    // No classes selected yet, prompt for course selection
                                    fetchClassesAndShowDialog(adapter);
                                }
                            } else {
                                Log.e(TAG, "No such document");
                                Toast.makeText(TeacherActivity.this, "No user data found. Please contact support.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error fetching user data: ", task.getException());
                            Toast.makeText(TeacherActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(TeacherActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchClassesAndShowDialog(ArrayAdapter<String> adapter) {
        Log.d(TAG, "Fetching all possible classes from Firestore");
        db.collection("Classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<String> allClasses = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String className = document.getString("name");
                    if (className != null) {
                        allClasses.add(className);
                    }
                }
                Log.d(TAG, "All classes fetched successfully: " + allClasses);

                // Save allClasses to be used in dialog
                classList.clear();
                classList.addAll(allClasses);

                // Show class selection dialog
                showClassSelectionDialog(adapter);
            } else {
                Log.e(TAG, "Error fetching classes: ", task.getException());
                Toast.makeText(TeacherActivity.this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClassSelectionDialog(ArrayAdapter<String> adapter) {
        if (classList.isEmpty()) {
            Toast.makeText(TeacherActivity.this, "No classes available to select.", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Classes");

        String[] classArray = classList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[classArray.length];
        ArrayList<String> selectedItems = new ArrayList<>();

        builder.setMultiChoiceItems(classArray, checkedItems, (dialog, which, isChecked) -> {
            if (isChecked) {
                selectedItems.add(classArray[which]);
            } else {
                selectedItems.remove(classArray[which]);
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            if (selectedItems.size() == 3) {
                saveSelectedClasses(selectedItems);
            } else {
                Toast.makeText(TeacherActivity.this, "Please select exactly 3 classes", Toast.LENGTH_SHORT).show();
                showClassSelectionDialog(adapter);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void saveSelectedClasses(List<String> selectedClasses) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid()).update("Classes", selectedClasses)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Classes updated successfully");
                        classList.clear();
                        classList.addAll(selectedClasses);
                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerClasses.getAdapter();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(TeacherActivity.this, "Classes selected successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating classes", e);
                        Toast.makeText(TeacherActivity.this, "Failed to update classes", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(TeacherActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
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
        String selectedClass = (String) spinnerClasses.getSelectedItem();
        Log.d(TAG, "Selected class: " + selectedClass);

        db.collection("Attendance").whereEqualTo("className", selectedClass).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String userEmail = document.getString("email");
                            String firstName = document.getString("firstName");
                            String lastName = document.getString("lastName");
                            String gpsPoints = document.getString("gpsPoints");

                            if (gpsPoints != null && !gpsPoints.isEmpty()) {
                                String[] gpsPointsArray = gpsPoints.split(", ");
                                double studentLatitude = Double.parseDouble(gpsPointsArray[0]);
                                double studentLongitude = Double.parseDouble(gpsPointsArray[1]);

                                float[] results = new float[1];
                                Location.distanceBetween(teacherLatitude, teacherLongitude, studentLatitude, studentLongitude, results);
                                double distance = Math.round(results[0] * 100.0) / 100.0;
                                String attendance = distance < 10 ? "Present" : "Absent";

                                String fullName = firstName + " " + lastName;
                                studentList.add(new Student(fullName, gpsPoints, distance, attendance));
                                Log.d(TAG, "Student added: " + fullName + ", " + gpsPoints + ", " + distance + ", " + attendance);
                            } else {
                                Log.d(TAG, "Student " + userEmail + " has no GPS points");
                            }
                        }
                        studentAdapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "Error getting attendance records: ", task.getException());
                        Toast.makeText(TeacherActivity.this, "Failed to fetch student data", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
