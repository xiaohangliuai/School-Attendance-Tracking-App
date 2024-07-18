package com.example.showattendance;


import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StudentActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView textViewLocation;
    private Button buttonShowLocation, buttonAddClasses;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Button buttonSignOut;

    private FloatingActionButton fabAddClass;
    private RecyclerView recyclerViewClass;
    ClassAdapter classAdapterClass;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<ClassItem> classItems = new ArrayList<>();
    EditText class_edt;
    EditText CRNs_edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        textViewLocation = findViewById(R.id.textViewLocation);
        buttonShowLocation = findViewById(R.id.buttonShowLocation);
        buttonSignOut = findViewById(R.id.buttonSignOut);
        buttonAddClasses = findViewById(R.id.buttonAddClasses);
        fabAddClass = findViewById(R.id.fabAddClass);

        recyclerViewClass = findViewById(R.id.recyclerViewClass);
        recyclerViewClass.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerViewClass.setLayoutManager(layoutManager);

        classAdapterClass = new ClassAdapter(this, classItems);
        recyclerViewClass.setAdapter(classAdapterClass);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonShowLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(StudentActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                getAndUpdateLocation();
            }
        });


        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        buttonAddClasses.setOnClickListener(v -> showDialog());
    }

    private void  showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.class_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        class_edt = view.findViewById(R.id.editTextClassName);
        CRNs_edit = view.findViewById(R.id.editTextCRNs);

        Button cancel = view.findViewById(R.id.buttonCancel);
        Button add = view.findViewById(R.id.buttonAdd);

        cancel.setOnClickListener(v -> dialog.dismiss());
        add.setOnClickListener(v -> {
            addClass();
            dialog.dismiss();
        });


    }

    private void addClass() {
        String className = class_edt.getText().toString();
        int CRNs = Integer.parseInt(CRNs_edit.getText().toString());;
        classItems.add(new ClassItem(className, CRNs));
        classAdapterClass.notifyDataSetChanged();
    }

    private void getAndUpdateLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String gpsPoints = latitude + ", " + longitude;
                    textViewLocation.setText(gpsPoints);

                    FirebaseUser user = mAuth.getCurrentUser();

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("gpsPoints", gpsPoints);

                    db.collection("Users").document(user.getUid())
                            .update(updates)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "User GPS points updated.");
                                } else {
                                    Log.e(TAG, "Error updating GPS points", task.getException());
                                }
                            });
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAndUpdateLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
