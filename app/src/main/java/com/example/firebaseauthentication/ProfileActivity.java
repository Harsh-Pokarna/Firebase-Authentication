package com.example.firebaseauthentication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity implements LocationListener {
    private EditText profileName, profileEmail, profileHobbies, userLatitude, userLongitude, userAddress;
    private String currentUserId, name, email, hobbies, imageUrl, address;
    Double latitude, longitude;
    private CircleImageView profile_image;
    private AppCompatButton updateUserInfo, logoutUser, userLocation;
    private final int galleryPick =1;

    private FirebaseAuth mAuth;
    private StorageReference profileImageRef;
    private DatabaseReference usersRef;
    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserId);
        profileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        initialiseFields();
        retrieveUserData();
        updateUserInfo.setOnClickListener(v -> updateUserProfile());
        profile_image.setOnClickListener(v -> {
            Intent galleryIntent = new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent, galleryPick);
        });
        logoutUser.setOnClickListener(v -> logout());
        userLocation.setOnClickListener(v -> findLocation());
    }

    private void findLocation() {
        if (ContextCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(ProfileActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        else {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 5, ProfileActivity.this);
        }

    }

    private void logout() {
        mAuth.signOut();
        sendUserToMainActivity();

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(mainIntent);
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
                    imageUrl = uri.toString();
                    usersRef.child("Image").setValue(imageUrl)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Picasso.get()
                                            .load(imageUrl)
                                            .placeholder(R.drawable.profile_image)
                                            .into(profile_image);
                                }
                                else {
                                    String message = task.getException().toString();
                                    Toast.makeText(ProfileActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                }

                            });
                }));


            }
        }
    }

    private void retrieveUserData() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Toast.makeText(ProfileActivity.this, "Done", Toast.LENGTH_SHORT).show();
                    name = snapshot.child("Name").getValue().toString();
                    email = snapshot.child("Email").getValue().toString();
                    hobbies = snapshot.child("Hobbies").getValue().toString();
                    imageUrl = snapshot.child("Image").getValue().toString();
                    profileHobbies.setText(hobbies);
                    profileName.setText(name);
                    profileEmail.setText(email);
                    Picasso.get()
                            .load(imageUrl)
                            .placeholder(R.drawable.profile_image)
                            .into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void updateUserProfile() {
        HashMap hashMap = new HashMap();
        hashMap.put("Email", profileEmail.getText().toString());
        hashMap.put("Hobbies", profileHobbies.getText().toString());
        hashMap.put("Image", imageUrl);
        hashMap.put("Name", profileName.getText().toString());
        usersRef.updateChildren(hashMap).addOnSuccessListener(o -> {
            Toast.makeText(this, "Updated Successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void initialiseFields() {
        profileEmail = findViewById(R.id.email);
        profileName = findViewById(R.id.name);
        profileHobbies = findViewById(R.id.hobbies);
        profile_image = findViewById(R.id.user_profile_image);
        updateUserInfo = findViewById(R.id.update_user_profile);
        logoutUser = findViewById(R.id.logout_user);
        userLocation = findViewById(R.id.user_location);
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        try {
            Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude,1);
            address = addresses.get(0).getAddressLine(0);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final View popupView = getLayoutInflater().inflate(R.layout.location_popup,null);
            userLatitude = popupView.findViewById(R.id.latitude);
            userLongitude = popupView.findViewById(R.id.longitude);
            userAddress = popupView.findViewById(R.id.address);
            userAddress.setText(address);
            userLongitude.setText(longitude.toString());
            userLatitude.setText(latitude.toString());
            builder.setView(popupView);
            AlertDialog dialog = builder.create();
            dialog.show();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}