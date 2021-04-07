package com.example.firebaseauthentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileName, profileEmail, profileHobbies;
    private String currentUserId, name, email, hobbies;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        profileEmail = findViewById(R.id.email);
        profileName = findViewById(R.id.name);
        profileHobbies = findViewById(R.id.hobbies);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Toast.makeText(ProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    name = snapshot.child("Name").getValue().toString();
                    email = snapshot.child("Email").getValue().toString();
                    hobbies = snapshot.child("Hobbies").getValue().toString();
                    profileHobbies.setText(hobbies);
                    profileName.setText(name);
                    profileEmail.setText(email);
                    Toast.makeText(ProfileActivity.this, "shown", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}