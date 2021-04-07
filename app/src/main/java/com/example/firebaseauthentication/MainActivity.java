package com.example.firebaseauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private EditText userEmail, userPassword;
    private AppCompatButton loginBtn, createNewAccountBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialiseFields();
        mAuth = FirebaseAuth.getInstance();
        loginBtn.setOnClickListener(v -> loginUser());
        createNewAccountBtn.setOnClickListener(v -> {
            Intent registerIntent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(registerIntent);
        });
    }

    private void loginUser() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    Toast.makeText(this, "Successfull", Toast.LENGTH_SHORT).show();
                    sendUserToProfileActivity();
                }
                else {
                    String message = task.getException().toString();
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }

            });
        }

    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);
        startActivity(profileIntent);

    }

    private void initialiseFields() {
        userEmail = findViewById(R.id.userEmailID);
        userPassword = findViewById(R.id.userPassword);
        loginBtn = findViewById(R.id.login_button);
        createNewAccountBtn = findViewById(R.id.createNewAccount);
    }
}