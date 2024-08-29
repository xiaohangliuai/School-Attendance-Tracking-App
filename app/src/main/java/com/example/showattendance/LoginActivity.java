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

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail, editTextPassword;
    private RadioGroup radioGroupRole;
    private Button buttonLogin;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        radioGroupRole = findViewById(R.id.radioGroupRole);
        buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonBack = findViewById(R.id.buttonBack);

        mAuth = FirebaseAuth.getInstance();

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
                                    // Login success
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                                    if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                        // Email is verified, proceed with role-based navigation
                                        if (role.equals("Student")) {
                                            Intent intent = new Intent(LoginActivity.this, StudentActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (role.equals("Teacher")) {
                                            Intent intent = new Intent(LoginActivity.this, TeacherActivity.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    } else {
                                        // Email is not verified, prompt user to verify email
                                        mAuth.signOut();
                                        Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            } else {
                Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });



        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });
    }
}
