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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends AppCompatActivity {

    private static final String TAG = "StudentActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Spinner spinnerClasses;
    private TextView textViewSignInStatus;
    private Button buttonSignIn, buttonSignOut;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private List<String> classList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        spinnerClasses = findViewById(R.id.spinnerClasses);
        textViewSignInStatus = findViewById(R.id.textViewSignInStatus);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignOut = findViewById(R.id.buttonSignOut);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        classList = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerClasses.setAdapter(adapter);

        fetchClasses(adapter);

        buttonSignIn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StudentActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                signInToClass();
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });


    }

    private void fetchClasses(ArrayAdapter<String> adapter) {
        Log.d(TAG, "Fetching classes from Firestore");
        db.collection("Classes").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                classList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String className = document.getString("name");
                    if (className != null) {
                        classList.add(className);
                    }
                }
                Log.d(TAG, "Classes fetched successfully: " + classList);
                adapter.notifyDataSetChanged();
            } else {
                Log.e(TAG, "Error fetching classes: ", task.getException());
                Toast.makeText(StudentActivity.this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void signInToClass() {
        Log.d(TAG, "Attempting to get location");
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Log.d(TAG, "Location obtained: " + location.toString());
                String selectedClass = (String) spinnerClasses.getSelectedItem();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String gpsPoints = latitude + ", " + longitude;

                // Save attendance record to Firestore
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userEmail = currentUser.getEmail();
                    db.collection("Users").document(currentUser.getUid()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                if (documentSnapshot.exists()) {
                                    String firstName = documentSnapshot.getString("firstName");
                                    String lastName = documentSnapshot.getString("lastName");

                                    AttendanceRecord record = new AttendanceRecord(userEmail, selectedClass, gpsPoints, firstName, lastName);
                                    db.collection("Attendance").add(record).addOnSuccessListener(documentReference -> {
                                        Log.d(TAG, "Attendance record added: " + documentReference.getId());
                                        textViewSignInStatus.setText("Your GPS Address: " + gpsPoints + "\nYou have signed in " + selectedClass + " successfully.");
                                        textViewSignInStatus.setVisibility(View.VISIBLE);
                                    }).addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to add attendance record", e);
                                        Toast.makeText(StudentActivity.this, "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Log.e(TAG, "User document does not exist");
                                    Toast.makeText(StudentActivity.this, "User information not found. Please contact support.", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to fetch user information", e);
                                Toast.makeText(StudentActivity.this, "Error fetching user information. Try again.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e(TAG, "User is not authenticated");
                    Toast.makeText(StudentActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Location is null");
                Toast.makeText(StudentActivity.this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void signInToClass() {
//        Log.d(TAG, "Attempting to get location");
//        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//            if (location != null) {
//                Log.d(TAG, "Location obtained: " + location.toString());
//                String selectedClass = (String) spinnerClasses.getSelectedItem();
//                double latitude = location.getLatitude();
//                double longitude = location.getLongitude();
//                String gpsPoints = latitude + ", " + longitude;
//
//                // Save attendance record to Firestore
//                FirebaseUser currentUser = mAuth.getCurrentUser();
//                if (currentUser != null) {
//                    String userEmail = currentUser.getEmail();
//                    AttendanceRecord record = new AttendanceRecord(userEmail, selectedClass, gpsPoints);
//                    db.collection("Attendance").add(record).addOnSuccessListener(documentReference -> {
//                        Log.d(TAG, "Attendance record added: " + documentReference.getId());
//                        textViewSignInStatus.setText("Your GPS Address: " + gpsPoints + "\nYou have signed in successfully.");
//                        textViewSignInStatus.setVisibility(View.VISIBLE);
//                    }).addOnFailureListener(e -> {
//                        Log.e(TAG, "Failed to add attendance record", e);
//                        Toast.makeText(StudentActivity.this, "Sign in failed. Try again.", Toast.LENGTH_SHORT).show();
//                    });
//                } else {
//                    Log.e(TAG, "User is not authenticated");
//                    Toast.makeText(StudentActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
//                }
//            } else {
//                Log.e(TAG, "Location is null");
//                Toast.makeText(StudentActivity.this, "Could not get location. Try again.", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                signInToClass();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}






//package com.example.showattendance;
//
//
//import static android.content.ContentValues.TAG;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AlertDialog;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.android.gms.tasks.Task;
//import com.google.android.material.floatingactionbutton.FloatingActionButton;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserProfileChangeRequest;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//public class StudentActivity extends AppCompatActivity {
//    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
//
//    private TextView textViewLocation;
//    private Button buttonShowLocation, buttonAddClasses;
//    private FusedLocationProviderClient fusedLocationClient;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore db;
//    private Button buttonSignOut, buttonSelectClass;
//
//    private FloatingActionButton fabAddClass;
//    private RecyclerView recyclerViewClass;
//    ClassAdapter classAdapterClass;
//    RecyclerView.LayoutManager layoutManager;
//    ArrayList<ClassItem> classItems = new ArrayList<>();
//    EditText class_edt;
//    EditText CRNs_edit;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_student);
//
//        textViewLocation = findViewById(R.id.textViewLocation);
//        buttonShowLocation = findViewById(R.id.buttonShowLocation);
//        buttonSignOut = findViewById(R.id.buttonSignOut);
//        buttonSelectClass = findViewById(R.id.buttonSelectClass);
////        buttonAddClasses = findViewById(R.id.buttonAddClasses);
////        fabAddClass = findViewById(R.id.fabAddClass);
////
////        recyclerViewClass = findViewById(R.id.recyclerViewClass);
////        recyclerViewClass.setHasFixedSize(true);
////        layoutManager = new LinearLayoutManager(this);
////        recyclerViewClass.setLayoutManager(layoutManager);
////
////        classAdapterClass = new ClassAdapter(this, classItems);
////        recyclerViewClass.setAdapter(classAdapterClass);
//
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        mAuth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        buttonShowLocation.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(StudentActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
//            } else {
//                getAndUpdateLocation();
//            }
//        });
//
//
//        buttonSignOut.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(StudentActivity.this, MainActivity.class);
//                startActivity(intent);
//
//            }
//        });
//
//        buttonSelectClass.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showSelectClassDialog();
//            }
//        });
//
////        buttonAddClasses.setOnClickListener(v -> showDialog());
//    }
//
//    private void getAndUpdateLocation() {
//        try {
//            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
//                if (location != null) {
//                    double latitude = location.getLatitude();
//                    double longitude = location.getLongitude();
//                    String gpsPoints = latitude + ", " + longitude;
//                    textViewLocation.setText(gpsPoints);
//
//                    FirebaseUser user = mAuth.getCurrentUser();
//
//                    Map<String, Object> updates = new HashMap<>();
//                    updates.put("gpsPoints", gpsPoints);
//
//                    db.collection("Users").document(user.getUid())
//                            .update(updates)
//                            .addOnCompleteListener(task -> {
//                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "User GPS points updated.");
//                                } else {
//                                    Log.e(TAG, "Error updating GPS points", task.getException());
//                                }
//                            });
//                }
//            });
//        } catch (SecurityException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void showSelectClassDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Select Class");
//
//        //
//        final List<String> classList = new ArrayList<>();
//        classList.add("Machine Learning 1234");
//        classList.add("Neural Network 1236");
//        classList.add("NLP 1235");
//        classList.add("Independent Study 1237");
//
//
//        String[] classArray = new String[classList.size()];
//        classList.toArray(classArray);
//
//        builder.setItems(classArray, (dialog, which) -> {
//            String selectedClass = classList.get(which);
//            Toast.makeText(StudentActivity.this, "Selected: " + selectedClass, Toast.LENGTH_SHORT).show();
//            //
//            Intent intent = new Intent(StudentActivity.this, ClassHomeActivity.class);
//            intent.putExtra("className", selectedClass);
//            startActivity(intent);
//        });
//
//        builder.create().show();
//    }
//
//
//
////
////    private void  showDialog(){
////        AlertDialog.Builder builder = new AlertDialog.Builder(this);
////        View view = LayoutInflater.from(this).inflate(R.layout.class_dialog, null);
////        builder.setView(view);
////        AlertDialog dialog = builder.create();
////        dialog.show();
////
////        class_edt = view.findViewById(R.id.editTextClassName);
////        CRNs_edit = view.findViewById(R.id.editTextCRNs);
////
////        Button cancel = view.findViewById(R.id.buttonCancel);
////        Button add = view.findViewById(R.id.buttonAdd);
////
////        cancel.setOnClickListener(v -> dialog.dismiss());
////        add.setOnClickListener(v -> {
////            addClass();
////            dialog.dismiss();
////        });
////
////
////    }
////
////    private void addClass() {
////        String className = class_edt.getText().toString();
////        int CRNs = Integer.parseInt(CRNs_edit.getText().toString());;
////        classItems.add(new ClassItem(className, CRNs));
////        classAdapterClass.notifyDataSetChanged();
////    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                getAndUpdateLocation();
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//}
