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


import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.Profile;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonRegister;
    private EditText regEmail;
    private EditText regPassword;
    private TextView textViewSignUp;
    private TextView fbLoginResultText;

    private ProgressDialog progressDialog;
    private FirebaseAuth firebase;
    private Intent loginIntent, profileIntent;

    private static final String TAG = "MyActivity";
    private static final String EMAIL = "email";
    private static final int RC_SIGN_IN = 9001;
    public boolean fbLogin = false;

    SignInButton googleSignIn;
    GoogleSignInClient mGoogleSignInClient;

    LoginButton fbLoginButton;
    CallbackManager fbCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressDialog = new ProgressDialog(this);
        firebase = firebase.getInstance();

        loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);

        buttonRegister = (Button) findViewById(R.id.register);
        regEmail = (EditText) findViewById(R.id.regEmail);
        regPassword = (EditText) findViewById(R.id.regPassword);
        textViewSignUp = (TextView) findViewById(R.id.alreadyRegistered);
        googleSignIn = (SignInButton) findViewById(R.id.google_sign_in);


        // calling listeners
        buttonRegister.setOnClickListener(this);
        textViewSignUp.setOnClickListener(this);
        googleSignIn.setOnClickListener(this);

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Facebook login

        fbLoginButton = (LoginButton) findViewById(R.id.login_button);
        fbCallbackManager = CallbackManager.Factory.create();
        fbLoginButton.setReadPermissions("email", "public_profile");
        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        fbLoginButton.registerCallback(fbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                //Toast.makeText(getApplicationContext(), "Fb Sign In successfull!.",Toast.LENGTH_SHORT).show();

                String fbUserId = loginResult.getAccessToken().getUserId();

                GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        displayUserInfo(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "first_name, last_name, email, id");
                graphRequest.setParameters(parameters);
                graphRequest.executeAsync();

            }

            @Override
            public void onCancel() {
                // App code
                Toast.makeText(getApplicationContext(), "Fb Sign In cancelled!.",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Toast.makeText(getApplicationContext(), "Fb Sign In Error!.",Toast.LENGTH_SHORT).show();
            }
        });


    }

    // Facebook sign in method

    public void displayUserInfo(JSONObject object){
        String first_name, last_name, email, id;
        try {
            first_name = object.getString("first_name");
            last_name = object.getString("last_name");
            email = object.getString("email");
            id = object.getString("id");

            Intent main = new Intent(MainActivity.this, ProfileActivity.class);
            main.putExtra("first_name", first_name);
            main.putExtra("last_name", last_name);
            main.putExtra("email", email);
            main.putExtra("id", id);
            startActivity(main);

            Toast.makeText(getApplicationContext(), "Fb Sign In successfull!."+ first_name +last_name +email + id,Toast.LENGTH_SHORT).show();
        }
        catch(JSONException e){
            e.printStackTrace();
        }

    }


    // Firebase user resister

    private void registerUser(){
        String email = regEmail.getText().toString().trim();
        String password = regPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            // email is empty.
            Toast.makeText(this, "Please enter an email.",Toast.LENGTH_SHORT).show();
            // stop further execution
            return;
        }

        if(TextUtils.isEmpty(password)){
            // password is empty.
            Toast.makeText(this, "Please enter password.", Toast.LENGTH_SHORT).show();
            // Stop further execution.
            return;
        }

        // if validation is successfull then show progress bar.
        progressDialog.setMessage("Registration in progress...");
        progressDialog.show();

        // Call firebase register method

        firebase.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            progressDialog.cancel();
                            // registration successfull
                            // start profile activity here
                            // for now just display Toast
                            Toast.makeText(MainActivity.this, "User registered successfully!", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            progressDialog.cancel();
                            // Something went wrong
                            Toast.makeText(MainActivity.this, "Something went wrong. Please try again!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    @Override
    public void onStart() {
        super.onStart();

        // If user is already signed in then sign him out.
        if(firebase.getCurrentUser() != null){
            firebase.signOut();
        }
    }

    // Google sign in method

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    // Google sign in - onActivityResult method

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        Log.d(TAG, "request code :" + requestCode);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }


    // Google sign in - handleSignInResult method

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            Toast.makeText(MainActivity.this, "Google sign in successfull!", Toast.LENGTH_SHORT).show();
            firebaseAuthWithGoogle(account);

        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(MainActivity.this, "Google sign in failed!", Toast.LENGTH_SHORT).show();
        }
    }

    // Google sign in - firebaseAuthWithGoogle method

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebase.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = firebase.getCurrentUser();

                            finish();
                            startActivity(profileIntent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                        // ...
                    }
                });
    }


    // method onClick

    @Override
    public void onClick(View view) {

        if(view == buttonRegister){
            registerUser();
        }

        if(view == textViewSignUp){
            // open sign in activity.
            progressDialog.cancel();
            Toast.makeText(MainActivity.this, "Clicked on login link!", Toast.LENGTH_SHORT).show();

            // start login activity
            finish();
            startActivity(loginIntent);
        }

        if(view == googleSignIn){
            signIn();
        }



    }
}
