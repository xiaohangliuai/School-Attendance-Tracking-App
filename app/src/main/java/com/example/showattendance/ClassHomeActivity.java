package com.example.showattendance;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ClassHomeActivity extends AppCompatActivity {

    private TextView textViewClassName;
    private Button buttonSignIn;
    private TextView textViewSignInStatus;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_home);

        TextView textViewClassName = findViewById(R.id.textViewClassName);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        textViewSignInStatus = findViewById(R.id.textViewSignInStatus);

        String className = getIntent().getStringExtra("className");
        textViewClassName.setText(className);


        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textViewSignInStatus.setText("You have completed the sign-in！");
            }
        });
    }
}
