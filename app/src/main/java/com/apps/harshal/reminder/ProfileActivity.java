package com.apps.harshal.reminder;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity{

    private FirebaseAuth firebase;
    private Intent loginIntent, regIntent;
    GoogleSignInAccount account;
    GoogleSignInClient mGoogleSignInClient;

    private static final String TAG = "ProfileActivity";

    private Button logOut;
    private TextView userEmail, userName;

    @Override
    public void onStart() {
        super.onStart();

    }

        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        logOut = (Button) findViewById(R.id.logOut);
        userEmail = (TextView) findViewById(R.id.userEmail);
        userName = (TextView) findViewById(R.id.userName);

        loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        regIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Firebase login with email and password

        firebase = firebase.getInstance();

        if(firebase.getCurrentUser() != null) {
            FirebaseUser firebaseUser = firebase.getCurrentUser();
            userEmail.setText("Welcome " + firebaseUser.getEmail());
        }


        // If no user is logged in the go back to login activity

        if(firebase.getCurrentUser() == null && LoginManager.getInstance() == null){
            finish();
            startActivity(regIntent);
        }

        // Fetch facebook profile data

        if(firebase.getCurrentUser() == null){
            Bundle inBundle = getIntent().getExtras();
            String first_name = inBundle.get("first_name").toString();
            String last_name = inBundle.get("last_name").toString();
            String email = inBundle.get("email").toString();
            String id = inBundle.get("id").toString();
            String gender = inBundle.get("gender").toString();
            String age_range = inBundle.get("age_range").toString();
            //String imageUrl = inBundle.get("imageUrl").toString();

            userEmail.setText("Welcome " + email );
            userName.setText("first_name:" + first_name +
                     "\nlast_name:" + last_name +
                    "\nemail:" + email +
                    "\ngender:" + gender+
                    "\nage_range:" + age_range
            );
        }

        // Log out button listener

        logOut.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                if(view == logOut) {

                    // Log out
                    Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();

                    // Sign out for firebase user & google user logged in with email and password

                    if(firebase.getCurrentUser() != null) {
                        firebase.signOut();
                    }

                    if(LoginManager.getInstance() != null) {
                        LoginManager.getInstance().logOut();
                    }

                    finish();
                    startActivity(regIntent);
                }
            }
        });
    }


}
