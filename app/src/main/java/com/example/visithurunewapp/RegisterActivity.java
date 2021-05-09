package com.example.visithurunewapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    Button CreateAccountButton;
    EditText UserEmail, UserPassword, UserRePassword;
    TextView AlreadyHaveAccount;
    ProgressDialog loadingBar;

    FirebaseAuth mAuth;
    DatabaseReference dbRefW;
    ChatUser newChatUser = new ChatUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        InitializeField();

        AlreadyHaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getApplicationContext(), "Clicked Button", Toast.LENGTH_SHORT).show();
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount() {

        dbRefW = FirebaseDatabase.getInstance().getReference().child("ChatUser");

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();


        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Password", Toast.LENGTH_SHORT).show();
        }
        else {
//            newChatUser.setEmailW(email);
//            newChatUser.setPasswordW(password);
//            newChatUser.setUserNameW("type user Name");
//            newChatUser.setStatusW("Hey I am Online");

            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait , while we are creating new Account for you..");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
//                                dbRefW.push().setValue(newChatUser);
                                Toast.makeText(RegisterActivity.this, "Account Created successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                                sendUserToMainActivity();
                            }
                            else {
                                String massage = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : " + massage, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void InitializeField() {

        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPassword = (EditText) findViewById(R.id.register_password);
        UserRePassword = (EditText) findViewById(R.id.re_register_password);
        AlreadyHaveAccount = (TextView) findViewById(R.id.register_using);

        loadingBar  = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}