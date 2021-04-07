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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.zip.InflaterInputStream;

public class RegisterActivity extends AppCompatActivity {
    private EditText registerEmail, registerPassword, registerHobbies, registerName;
    AppCompatButton createNewAccountButton, final_register;
    private String currentUserId, name, email, password, hobbies;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialiseFields();
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        createNewAccountButton.setOnClickListener(v -> {
            createAccount();
            registerEmail.setVisibility(View.INVISIBLE);
            registerPassword.setVisibility(View.INVISIBLE);
            createNewAccountButton.setVisibility(View.INVISIBLE);
            registerHobbies.setVisibility(View.VISIBLE);
            final_register.setVisibility(View.VISIBLE);
            registerName.setVisibility(View.VISIBLE);

        });
        final_register.setOnClickListener(v -> {
            addHobbies();
            sendUserToProfileActivity();

        });
    }

    private void addHobbies() {
        name = registerName.getText().toString();
        hobbies = registerHobbies.getText().toString();
        if (TextUtils.isEmpty(hobbies)){
            Toast.makeText(this, "Please enter your hobbies", Toast.LENGTH_SHORT).show();
        }
        else {
            usersRef.child("Hobbies").setValue(hobbies);
            usersRef.child("Name").setValue(name);
            usersRef.child("Email").setValue(email);
            sendUserToProfileActivity();

        }
    }

    private void createAccount() {
        email = registerEmail.getText().toString().trim();
        password = registerPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter Email..", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter Password..", Toast.LENGTH_SHORT).show();
        }
        else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                if (task.isSuccessful()){
                    currentUserId = mAuth.getCurrentUser().getUid();
                    rootRef.child("Users").child(currentUserId).setValue("");
                    usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
                    Toast.makeText(this, "Account Created Successfully", Toast.LENGTH_SHORT).show();
                }
                else {
                    String message = task.getException().toString();
                    Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }

            });

        }

    }

    private void sendUserToProfileActivity() {
        Intent profileIntent = new Intent(RegisterActivity.this, ProfileActivity.class);;
        startActivity(profileIntent);
    }

    private void initialiseFields() {
        registerEmail = findViewById(R.id.register_userEmailID);
        registerPassword = findViewById(R.id.register_userPassword);
        createNewAccountButton = findViewById(R.id.register_button);
        registerHobbies = findViewById(R.id.register_hobbies);
        final_register = findViewById(R.id.register_button_final);
        registerName = findViewById(R.id.register_name);
    }
}