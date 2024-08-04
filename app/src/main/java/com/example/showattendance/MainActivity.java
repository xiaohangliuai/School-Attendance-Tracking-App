package com.example.showattendance;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Button buttonLogin;
    private TextView tvRegister;
    private EditText editTextEmail, editTextPassword;
    private RadioGroup radioGroupRole;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLogin = findViewById(R.id.buttonLogin);
        tvRegister = findViewById(R.id.buttonRegister);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            RadioButton radioButtonRole = findViewById(selectedRoleId);
            String role = radioButtonRole.getText().toString();

            if (!email.isEmpty() && !password.isEmpty() && selectedRoleId != -1 && email.toLowerCase().endsWith("yu.edu")) {

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {

                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (role.equals("Student")) {
                                        Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                                        startActivity(intent);
                                        finish();
                                    } else if (role.equals("Teacher")) {
                                        Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();

                                }
                            }
                        });


//                mAuth.signInWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
////                        .addOnCompleteListener(this, task -> {
//                                if (task.isSuccessful()) {
//                                    Log.d(TAG, "signInWithEmail:success");
//                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
//
//                                    if (firebaseUser != null) {
//                                        String userId = firebaseUser.getUid();
//                                        Log.d(TAG, "User ID: " + userId);
//                                        DocumentReference docRef = db.collection("users").document(userId);
//
//                                        docRef.get().addOnCompleteListener(task1 -> {
//                                            if (task1.isSuccessful()) {
//                                                DocumentSnapshot document = task1.getResult();
//                                                Log.d(TAG, "document: " + document);
//                                                if (document.exists()) {
//                                                    String role = document.getString("role");
//                                                    if (role != null) {
//                                                        if (role.equals("Student")) {
//                                                            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
//                                                            startActivity(intent);
//                                                            finish();
//                                                        } else if (role.equals("Teacher")) {
//                                                            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
//                                                            startActivity(intent);
//                                                            finish();
//                                                        }
//                                                    } else {
//                                                        Log.d(TAG, "No role information found.");
//                                                        Toast.makeText(MainActivity.this, "Role information missing.", Toast.LENGTH_SHORT).show();
//                                                    }
//                                                } else {
//                                                    Log.d(TAG, "No such document");
//                                                    Toast.makeText(MainActivity.this, "User document not found.", Toast.LENGTH_SHORT).show();
//                                                }
//                                            } else {
//                                                Log.d(TAG, "get failed with ", task1.getException());
//                                                Toast.makeText(MainActivity.this, "Error getting user document.", Toast.LENGTH_SHORT).show();
//                                            }
//                                        });
//                                    }
//                                } else {
//                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                    Toast.makeText(MainActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });


            } else {
                Toast.makeText(MainActivity.this, "Please input the correct information", Toast.LENGTH_SHORT).show();
            }
        });


        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });


    }
}
