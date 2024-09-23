//package com.example.showattendance;
//
//import static android.content.ContentValues.TAG;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.RadioButton;
//import android.widget.RadioGroup;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.OnCompleteListener;
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.auth.AuthResult;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//public class MainActivity extends AppCompatActivity {
//    private Button buttonLogin;
//    private TextView tvRegister;
//    private EditText editTextEmail, editTextPassword;
//    private RadioGroup radioGroupRole;
//
//    private FirebaseAuth mAuth;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        buttonLogin = findViewById(R.id.buttonLogin);
//        tvRegister = findViewById(R.id.buttonRegister);
//
//        editTextEmail = findViewById(R.id.editTextEmail);
//        editTextPassword = findViewById(R.id.editTextPassword);
//        radioGroupRole = findViewById(R.id.radioGroupRole);
//
//        mAuth = FirebaseAuth.getInstance();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        buttonLogin.setOnClickListener(v -> {
//            String email = editTextEmail.getText().toString();
//            String password = editTextPassword.getText().toString();
//            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
//            RadioButton radioButtonRole = findViewById(selectedRoleId);
//            String role = radioButtonRole.getText().toString();
//
//            if (!email.isEmpty() && !password.isEmpty() && selectedRoleId != -1 && email.toLowerCase().endsWith("yu.edu")) {
//
//                mAuth.signInWithEmailAndPassword(email, password)
//                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(@NonNull Task<AuthResult> task) {
//                                if (task.isSuccessful()) {
//
//                                    Log.d(TAG, "signInWithEmail:success");
//                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
//                                    if (role.equals("Student")) {
//                                        Intent intent = new Intent(MainActivity.this, StudentActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    } else if (role.equals("Teacher")) {
//                                        Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
//                                        startActivity(intent);
//                                        finish();
//                                    }
//
//                                } else {
//                                    // If sign in fails, display a message to the user.
//                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
//                                    Toast.makeText(MainActivity.this, "Authentication failed.",
//                                            Toast.LENGTH_SHORT).show();
//
//                                }
//                            }
//                        });
//
//
//            } else {
//                Toast.makeText(MainActivity.this, "Please input the correct information", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//        tvRegister.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
//            startActivity(intent);
//        });
//
//
//    }
//}


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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private Button buttonLogin;
    private TextView tvRegister;
    private EditText editTextEmail, editTextPassword;
    private RadioGroup radioGroupRole;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

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
        db = FirebaseFirestore.getInstance();

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            int selectedRoleId = radioGroupRole.getCheckedRadioButtonId();
            RadioButton radioButtonRole = findViewById(selectedRoleId);
            String selectedRole = radioButtonRole.getText().toString();


            if (!email.isEmpty() && !password.isEmpty() && selectedRoleId != -1) {
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    assert firebaseUser != null;

                                    if (!firebaseUser.isEmailVerified()) {
                                        mAuth.signOut();
                                        Toast.makeText(MainActivity.this, "Please verify your email before logging in.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String userId = firebaseUser.getUid();
                                    db.collection("Users").document(userId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    String role = document.getString("role");
                                                    if (role != null) {
                                                        if (role.equals(selectedRole)) {
                                                            if ((role.equals("Teacher") && email.toLowerCase().endsWith("yu.edu"))
                                                                    || (role.equals("Student") && email.toLowerCase().endsWith("mail.yu.edu"))) {
                                                                navigateToRoleActivity(role);
                                                            } else {
                                                                Toast.makeText(MainActivity.this, "Email domain does not match the selected role.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        } else {
                                                            Toast.makeText(MainActivity.this, "Selected role does not match the user's role in the database.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } else {
                                                        Toast.makeText(MainActivity.this, "User role is not specified.", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else {
                                                    Toast.makeText(MainActivity.this, "User does not exist.", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Log.w(TAG, "Error getting user role", task.getException());
                                                Toast.makeText(MainActivity.this, "Failed to verify user role.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity.this, "Authentication failed. Please check your email and password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                Toast.makeText(MainActivity.this, "Please input the correct information", Toast.LENGTH_SHORT).show();
            }
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void navigateToRoleActivity(String role) {
        if (role.equals("Student")) {
            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
            startActivity(intent);
            finish();
        } else if (role.equals("Teacher")) {
            Intent intent = new Intent(MainActivity.this, TeacherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
