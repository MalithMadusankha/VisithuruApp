package com.example.visithurunewapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    String receiveUserID, sendUserID, Current_state;
    ImageView userProfileImage;
    TextView userProfileName, userProfileStatus;
    Button SendMassageButton;

//    String currentUserID;
    FirebaseAuth mAuth;
    DatabaseReference RootRef, ChatRequestRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        receiveUserID = getIntent().getExtras().get("visit_user_id").toString();
        Toast.makeText(this, "User ID :  " + receiveUserID,Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
//        currentUserID = mAuth.getCurrentUser().getUid();
        sendUserID = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();// didn't use child("ChatUser")
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");

        InitializeField();
        RetrieveUserInfo();



    }


    private void InitializeField() {
        userProfileImage = (ImageView) findViewById(R.id.visit_profile_image);
        userProfileName = (TextView) findViewById(R.id.visit_profile_name);
        userProfileStatus = (TextView) findViewById(R.id.visit_profile_status);
        SendMassageButton = (Button) findViewById(R.id.send_massage_request_button);
        Current_state = "new";
    }

    private void RetrieveUserInfo() {
        RootRef.child("ChatUser").child(receiveUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Log.i("LocalMassage","Retrive : ");
                        if((dataSnapshot.exists()) && (dataSnapshot.hasChild("userNameW")) && dataSnapshot.hasChild("image")){

                            String retrieveUserName = dataSnapshot.child("userNameW").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("userStatusW").getValue().toString();
                            String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
                            Log.i("LocalMassage","url : "+ retrieveProfileImage);
                            userProfileName.setText(retrieveUserName);
                            userProfileStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                            ManageChatRequest();

                        }
                        else {

                            String retrieveUserName = dataSnapshot.child("userNameW").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("userStatusW").getValue().toString();
                            Log.i("LocalMassage","else if : "+ retrieveStatus);

                            userProfileName.setText(retrieveUserName);
                            userProfileStatus.setText(retrieveStatus);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void ManageChatRequest() {
        if(sendUserID.equals(receiveUserID)) {
            SendMassageButton.setEnabled(false);
            if(Current_state.equals("new")){
                SendChatRequest();
            }
        }
        else {
            SendMassageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void SendChatRequest() {
        ChatRequestRef.child(sendUserID).child(receiveUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            ChatRequestRef.child(receiveUserID).child(sendUserID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                SendMassageButton.setEnabled(true);
                                                Current_state = "request_sent";
                                                SendMassageButton.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}