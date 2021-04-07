package com.example.firebaseauthentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;

import java.util.zip.InflaterInputStream;


public class RegisterActivity extends AppCompatActivity {
    private EditText registerEmail, registerPassword, registerHobbies, registerName;
    AppCompatButton createNewAccountButton, final_register;
    private String currentUserId, name, email, password, hobbies;
    private CircleImageView profile_image;
    private int galleryPick = 1;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef, usersRef;
    private StorageReference profileImageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialiseFields();
        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        createNewAccountButton.setOnClickListener(v -> {
            createAccount();
            registerEmail.setVisibility(View.INVISIBLE);
            registerPassword.setVisibility(View.INVISIBLE);
            createNewAccountButton.setVisibility(View.INVISIBLE);
            registerHobbies.setVisibility(View.VISIBLE);
            final_register.setVisibility(View.VISIBLE);
            registerName.setVisibility(View.VISIBLE);
            profile_image.setVisibility(View.VISIBLE);

        });
        profile_image.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, galleryPick);
        });
        final_register.setOnClickListener(v -> {
            addHobbies();
            sendUserToProfileActivity();

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
                Uri imageUri =data.getData();
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).
                        setAspectRatio(1,1).start(this);
            }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK){
                    Uri resultUri = result.getUri();
                    StorageReference filePath = profileImageRef.child(currentUserId + ".jpg");
                    filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                        final String downloadUrl = uri.toString();
                        usersRef.child("Image").setValue(downloadUrl)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "Image saved in database", Toast.LENGTH_LONG).show();
                                        Picasso.get()
                                                .load(downloadUrl)
                                                .placeholder(R.drawable.profile_image)
                                                .into(profile_image);
                                    }
                                    else {
                                        String message = task.getException().toString();
                                        Toast.makeText(RegisterActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                    }

                                });
                    }));


                }
            }
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
                    registerEmail.setVisibility(View.VISIBLE);
                    registerPassword.setVisibility(View.VISIBLE);
                    createNewAccountButton.setVisibility(View.VISIBLE);
                    registerHobbies.setVisibility(View.INVISIBLE);
                    final_register.setVisibility(View.INVISIBLE);
                    registerName.setVisibility(View.INVISIBLE);
                    profile_image.setVisibility(View.INVISIBLE);
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
        profile_image = findViewById(R.id.profile_image);
    }
}