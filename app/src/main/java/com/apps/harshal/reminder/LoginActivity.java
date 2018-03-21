package com.apps.harshal.reminder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonLogin;
    private EditText loginEmail;
    private EditText loginPassword;
    private TextView signUp;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebase;
    private Intent signUpIntent, profileIntent;

    private static final String TAG = "LoginActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonLogin = (Button) findViewById(R.id.login);
        loginEmail = (EditText) findViewById(R.id.loginEmail);
        loginPassword = (EditText) findViewById(R.id.loginPassword);
        signUp = (TextView) findViewById(R.id.signUp);

        signUpIntent = new Intent(getApplicationContext(), MainActivity.class);
        profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);

        progressDialog = new ProgressDialog(this);
        firebase = firebase.getInstance();

        buttonLogin.setOnClickListener(this);
        signUp.setOnClickListener(this);

    }

    private void loginUser(){

        String email = loginEmail.getText().toString().trim();
        String password = loginPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            // email is empty
            Toast.makeText(this, "Please enter an email.", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password)) {

            // password is empty
            Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show();
        }

        progressDialog.setMessage("Login in progress");
        progressDialog.show();

        firebase.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.cancel();
                            Toast.makeText(LoginActivity.this, "Login Successfull!", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = firebase.getCurrentUser();

                            // start the profile activity

                            finish();
                            startActivity(profileIntent);

                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.cancel();
                            Toast.makeText(LoginActivity.this, "Incorrect email or password! Please try again.", Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this, "Authentication failed.",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }


    @Override
    public void onClick(View view) {

        if(view == buttonLogin){
            loginUser();
        }

        if(view == signUp){
            // open sign in activity.
            progressDialog.cancel();
            Toast.makeText(LoginActivity.this, "Clicked on login link!", Toast.LENGTH_SHORT).show();

            // Start register activity
            finish();
            startActivity(signUpIntent);
        }
    }
}
