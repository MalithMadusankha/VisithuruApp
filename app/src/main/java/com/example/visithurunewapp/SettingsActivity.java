package com.example.visithurunewapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {


    Button UpdateAccountSettings;
    EditText userName, userStatus;
    ShapeableImageView userProfileImage;

    String currentUserID;
    FirebaseAuth mAuth;
    DatabaseReference RootRef;


    private static final int GalleryPick = 1;
//    Uri imageUri;
    StorageReference UserProfileImageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFielsds();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i("LocalMassage", "Image Clicked");
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });
    }

    private void RetrieveUserInfo() {
        RootRef.child("ChatUser").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                             if((dataSnapshot.exists()) && (dataSnapshot.hasChild("userNameW")) && dataSnapshot.hasChild("image")){

                                 String retrieveUserName = dataSnapshot.child("userNameW").getValue().toString();
                                 String retrieveStatus = dataSnapshot.child("userStatusW").getValue().toString();
                                 String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                                 userName.setText(retrieveUserName);
                                 userStatus.setText(retrieveStatus);

                             }
                                 else if((dataSnapshot.exists()) && (dataSnapshot.hasChild("userNameW"))){

                                 String retrieveUserName = dataSnapshot.child("userNameW").getValue().toString();
                                 String retrieveStatus = dataSnapshot.child("userStatusW").getValue().toString();


                                 userName.setText(retrieveUserName);
                                 userStatus.setText(retrieveStatus);

                                 }
                                     else {
                                        userName.setVisibility(View.VISIBLE);
                                         Toast.makeText(SettingsActivity.this,"Please update your profile Information", Toast.LENGTH_LONG).show();
                                    }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this,"Please Enter user name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this,"Please Enter Status", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, String> profileMap = new HashMap<>();
            profileMap.put("uid", currentUserID);
            profileMap.put("userNameW", setUserName);
            profileMap.put("userStatusW", setStatus);

            RootRef.child("ChatUser").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(SettingsActivity.this,"Profil Updated", Toast.LENGTH_SHORT).show();
                                sendUserToMainActivity();
                            }
                            else {
                                String massage = task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error : " + massage, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

        }
    }

    private void InitializeFielsds() {
        UpdateAccountSettings = (Button) findViewById(R.id.update_setting_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (ShapeableImageView) findViewById(R.id.set_profile_image);
    }

    //Image result
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == RESULT_OK) {
//            if (requestCode == GalleryPick) {
//
//                imageUri = data.getData();
//
//                try{
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
//                    userProfileImage.setImageBitmap(bitmap);
//
////                    Uri resultUri = imageUri.getU();
//
//                    StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg")
//                }catch (IOException e){
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GalleryPick && resultCode == RESULT_OK && data!=null){
            Uri ImageUri = data.getData();

            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Log.i("LocalMassage", "Image Clicked" + result);

            if(resultCode == RESULT_OK){
                Uri resultUre = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUre).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile Image Uploded", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            String massage = task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error : " + massage, Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
        }
    }


    private void sendUserToMainActivity() {
        Intent settingIntent = new Intent(SettingsActivity.this, MainActivity.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }
}