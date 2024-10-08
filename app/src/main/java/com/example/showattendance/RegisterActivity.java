package com.example.showattendance;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword, editTextFirstName, editTextLastName;
    private RadioGroup radioGroupRole;
    private Button buttonRegister;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonRegister = findViewById(R.id.buttonRegister);
        editTextFirstName = findViewById(R.id.editTextFirstName);
        editTextLastName = findViewById(R.id.editTextLastName);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonRegister.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            RadioButton radioButtonRole = findViewById(selectedRoleId);
            String role = radioButtonRole.getText().toString();
            String firstName = editTextFirstName.getText().toString();
            String lastName = editTextLastName.getText().toString();

            if (!email.isEmpty() && !password.isEmpty() && selectedRoleId != -1 && email.toLowerCase().endsWith("yu.edu")) {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    if (firebaseUser != null) {
                                        // Send email verification
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();

                                                            // Sign out the user to force email verification before login
                                                            mAuth.signOut();

                                                            // Store user info in Firestore
                                                            User user = new User(email, role, "", firstName, lastName);
                                                            db.collection("Users").document(firebaseUser.getUid()).set(user)
                                                                    .addOnSuccessListener(aVoid -> {
                                                                        Toast.makeText(RegisterActivity.this, "Registration successful. Please verify your email.", Toast.LENGTH_SHORT).show();
                                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                                        startActivity(intent);
                                                                        finish();
                                                                    })
                                                                    .addOnFailureListener(e -> {
                                                                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                                                                        Log.e(TAG, "Error adding document", e);
                                                                    });
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "sendEmailVerification", task.getException());
                                                        }
                                                    }
                                                });
                                    }

                                } else {
                                    // If sign in fails
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


//
//
//                mAuth.createUserWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//                                    // Sign in success, update UI with the signed-in user's information
//                                    Log.d(TAG, "createUserWithEmail:success");
//                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
//
//                                    if (firebaseUser != null) {
//                                        // Send email verification
//                                        firebaseUser.sendEmailVerification()
//                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<Void> task) {
//                                                        if (task.isSuccessful()) {
//                                                            Toast.makeText(RegisterActivity.this, "Verification email sent. Please check your inbox.", Toast.LENGTH_SHORT).show();
//
////                                                            // Sign out the user to force email verification before login
////                                                            mAuth.signOut();
//
//                                                            User user = new User(email, role, "", firstName, lastName);
//                                                            db.collection("Users").document(firebaseUser.getUid()).set(user)
//                                                                    .addOnSuccessListener(aVoid -> {
//                                                                        Toast.makeText(RegisterActivity.this, "Registration successful. Please verify your email.", Toast.LENGTH_SHORT).show();
//                                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
//                                                                        startActivity(intent);
//                                                                        finish();
//                                                                    })
//                                                                    .addOnFailureListener(e -> {
//                                                                        Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
//                                                                        Log.e(TAG, "Error adding document", e);
//                                                                    });
//                                                        } else {
//                                                            Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_SHORT).show();
//                                                            Log.e(TAG, "sendEmailVerification", task.getException());
//                                                        }
//                                                    }
//                                                });
//                                    }
//
//                                } else {
//                                    // If sign in fails
//                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                                    Toast.makeText(RegisterActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        });
            } else {
                Toast.makeText(RegisterActivity.this, "Please input the correct information", Toast.LENGTH_SHORT).show();
            }
        });
    }
}


