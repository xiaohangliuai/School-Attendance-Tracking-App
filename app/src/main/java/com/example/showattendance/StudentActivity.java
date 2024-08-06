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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    private List<String> selectedClasses;


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

        // Fetch user-specific classes
        fetchUserClasses(adapter);

        buttonSignIn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StudentActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                signInToClass();
            }
        });

        buttonSignOut.setOnClickListener(v -> {
            Intent intent = new Intent(StudentActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void fetchUserClasses(ArrayAdapter<String> adapter) {
        Log.d(TAG, "Fetching user classes from Firestore");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("Users").document(currentUser.getUid()).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            classList.clear();
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                List<String> userClasses = (List<String>) document.get("Classes");
                                if (userClasses != null && !userClasses.isEmpty()) {
                                    classList.addAll(userClasses);
                                    Log.d(TAG, "User classes fetched successfully: " + classList);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Log.d(TAG, "No classes registered for the user. Fetching all classes.");
                                    fetchClassesAndShowDialog(adapter);
                                }
                            } else {
                                Log.e(TAG, "No such document");
                                Toast.makeText(StudentActivity.this, "No registered classes found", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "Error fetching user classes: ", task.getException());
                            Toast.makeText(StudentActivity.this, "Failed to fetch user classes", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(StudentActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(StudentActivity.this, "Failed to fetch classes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showClassSelectionDialog(ArrayAdapter<String> adapter) {
        if (classList.isEmpty()) {
            // Classes not loaded yet, skip dialog
            Toast.makeText(StudentActivity.this, "No classes available to select.", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(StudentActivity.this, "Please select exactly 3 classes", Toast.LENGTH_SHORT).show();
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
                        this.selectedClasses = new ArrayList<>(selectedClasses);
                        Toast.makeText(StudentActivity.this, "Classes selected successfully", Toast.LENGTH_SHORT).show();
                        // Refresh user classes after saving
                        fetchUserClasses(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, classList));
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error updating classes", e);
                        Toast.makeText(StudentActivity.this, "Failed to update classes", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "User is not authenticated");
            Toast.makeText(StudentActivity.this, "User not authenticated. Please log in again.", Toast.LENGTH_SHORT).show();
        }
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





