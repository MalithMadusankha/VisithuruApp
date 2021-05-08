package com.example.visithurunewapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {


    Button UpdateAccountSettings;
    EditText userName, userStatus;
    ShapeableImageView userProfileImage;

    String currentUserID;
    FirebaseAuth mAuth;
    DatabaseReference RootRef;

    ProgressDialog loadingBar;
    String imageUrl;


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
                                 Log.i("LocalMassage","url : "+ retrieveProfileImage);
                                 userName.setText(retrieveUserName);
                                 userStatus.setText(retrieveStatus);
                                 Picasso.get().load(retrieveProfileImage).into(userProfileImage);

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
            if(imageUrl != null)
                profileMap.put("image", imageUrl);


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
        loadingBar = new ProgressDialog(this);
    }



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
                loadingBar.setTitle("Set profile Image");
                loadingBar.setMessage("Please wait, Your profile is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUre = result.getUri();

                StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
                
                // FromStack Overflow-----------------------------------------------------------------------


                final UploadTask uploadTask = filePath.putFile(resultUre);
                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    loadingBar.dismiss();
                                    throw task.getException();

                                }
                                // Continue with the task to get the download URL

                                return filePath.getDownloadUrl();

                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    String thumbDownloadUrl = task.getResult().toString(); // Found Url
                                    imageUrl = thumbDownloadUrl;

                                    RootRef.child("ChatUser").child(currentUserID).child("image")
                                    .setValue(thumbDownloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingsActivity.this,"Image Save in Database, Sucessfully...",Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else {
                                                String massage = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this," Error : " + massage,Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                                    Log.i("LocalMassage","url new : " + thumbDownloadUrl);


                                }
                            }
                        });

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });



                // My Code Image
//                filePath.putFile(resultUre).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
//                        if (task.isSuccessful()){
//                            Toast.makeText(SettingsActivity.this,"Profile Image uploaded Successfully", Toast.LENGTH_SHORT).show();
//                            final String downloaedUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
//                            Log.i("LocalMassage","Download Url :" + downloaedUrl);
//
//                            RootRef.child("ChatUser").child(currentUserID).child("image")
//                                    .setValue(downloaedUrl)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if(task.isSuccessful()){
//                                                Toast.makeText(SettingsActivity.this,"Image Save in Database, Sucessfully...",Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                            else {
//                                                String massage = task.getException().toString();
//                                                Toast.makeText(SettingsActivity.this," Error : " + massage,Toast.LENGTH_SHORT).show();
//                                                loadingBar.dismiss();
//                                            }
//                                        }
//                                    });
//                        }
//                        else {
//                            String massage = task.getException().toString();
//                            Toast.makeText(SettingsActivity.this,"Error : " + massage, Toast.LENGTH_SHORT).show();
//                            loadingBar.dismiss();
//
//                        }
//                    }
//                });
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