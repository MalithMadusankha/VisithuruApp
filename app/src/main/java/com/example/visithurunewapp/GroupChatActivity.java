package com.example.visithurunewapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

     Toolbar mToolbar;
     Button sendMassageButton;
     EditText userMassageInput;
     ScrollView mScrollView;
     TextView displayTextMassage, groupNameTextView;

     FirebaseAuth mAuth;
     DatabaseReference userRef, GroupNameRef, GroupMassageKeyRef;

     String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("ChatUser");
//        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();

        InitializeField();
        groupNameTextView.setText(currentGroupName);

        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        getUserInfo();

        sendMassageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMassageInfoToDatabase();
                userMassageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()){
                    DisplayMassage(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void DisplayMassage(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();

        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMassage.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n" );

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void SaveMassageInfoToDatabase() {
        String massage = userMassageInput.getText().toString();
        String massageKey = GroupNameRef.push().getKey();
        Log.i("LocalMassage", "SaveMassageInfoToDatabase : massage : " + massage);

        if(TextUtils.isEmpty(massage)){
            Toast.makeText(this, "Enter massage",Toast.LENGTH_SHORT).show();
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMassageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMassageKey);

            GroupMassageKeyRef = GroupNameRef.child(massageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("message", massage);
            Log.i("LocalMassage", "SaveMassageInfoToDatabase : Name : " + currentUserName);
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("date", currentDate);
            messageInfoMap.put("time", currentTime);

            GroupMassageKeyRef.updateChildren(messageInfoMap);



        }
    }

    private void getUserInfo() {
        userRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("userNameW").getValue().toString();
                    Log.i("LocalMassage", "getUserInfo" + currentUserName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void InitializeField() {
        mToolbar = (Toolbar) findViewById(R.id.group_chat_layout);
//        setSupportActionBar(mToolbar);
//        getSupportActionBar().setTitle("Group Name");
        groupNameTextView = (TextView) findViewById(R.id.group_name_text_view);
        sendMassageButton = (Button) findViewById(R.id.send_massage_button);
        userMassageInput = (EditText) findViewById(R.id.input_group_massage);
        displayTextMassage = (TextView) findViewById(R.id.group_chat_text_display);
        mScrollView = (ScrollView) findViewById(R.id.my_scroll_view);

    }
}