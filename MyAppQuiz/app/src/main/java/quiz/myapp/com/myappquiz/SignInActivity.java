package quiz.myapp.com.myappquiz;

/**
 * Created by venkatesh on 10/2/2017.
 */


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignInActivity extends AppCompatActivity
        implements View.OnClickListener{
    private final String TAG = "SIGNIN_ACT";
    private final int REQUEST_CODE=20,RESULT_CLOSE_APPLICATION=30;
    //private int requestCode = 10;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences mPrefs;
    private EditText etPass;
    private EditText etEmail;
    public ProgressBar signInWait;
    private int requestCode = 10;
    private boolean emailVerified=false;

    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");


        setContentView(R.layout.activity_sign_in);

        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignIn).setOnClickListener(this);
        //findViewById(R.id.btnSignOut).setOnClickListener(this);

        etEmail = (EditText)findViewById(R.id.etEmailAddr);
        etPass = (EditText)findViewById(R.id.etPassword);
        signInWait = (ProgressBar) findViewById(R.id.PBsignIn);

        // TODO: Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();

        // TODO: Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    // User is signed in

                    Log.d(TAG, "Signed in: " + user.getUid());

                    Toast.makeText(SignInActivity.this, "You are Signed in ", Toast.LENGTH_SHORT)
                            .show();

                    Intent beaconIntent = new Intent(SignInActivity.this, MainActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    beaconIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    beaconIntent.putExtra("USER_UID",mAuth.getCurrentUser().getUid());
                    beaconIntent.putExtra("USER_EMAIL",mAuth.getCurrentUser().getEmail());
                    //SignInActivity.this.startActivity(beaconIntent);
                    signInWait.setVisibility(View.GONE);
                    SignInActivity.this.startActivityForResult(beaconIntent,REQUEST_CODE);


                } else {
                    signInWait.setVisibility(View.GONE);
                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                    Toast.makeText(SignInActivity.this, "Please Sign in to Continue!!", Toast.LENGTH_SHORT)
                            .show();
                    ;
                }
            }
        };

        //updateStatus();
        mPrefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {

        Log.d(TAG,"onIntent Called");
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Log.d(TAG, "Signed in: " + user.getUid());

                  signUserOut();


                } else {
                    Log.d(TAG, "Currently signed out");
                    SignInActivity.this.finish();
                }
            }
        };

    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        if(mPrefs.getBoolean("FIRST_RUN",true)){
            Log.d("SIGN_ACT","Subscribing for Events");
            FirebaseMessaging.getInstance().subscribeToTopic("EVENTS");
            mPrefs.edit().putBoolean("FIRST_RUN", false).commit();
        }
    }
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_CODE) {
            Log.d(TAG, "Request Code:"+String.valueOf(requestCode));
            if (resultCode == RESULT_CLOSE_APPLICATION) {
                Log.d(TAG, "Result Code:"+String.valueOf(resultCode));
                this.finish();
            }
        }
    }


    /**
     * When the Activity starts and stops, the app needs to connect and
     * disconnect the AuthListener
     */
    @Override
    public void onStart() {
        Log.d(TAG,"onStart");
        super.onStart();
        // TODO: add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        Log.d(TAG,"onStop");
        super.onStop();
        // TODO: Remove the AuthListener
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSignIn:

                signInWait.setVisibility(View.VISIBLE);
                signUserIn();
                break;

           case R.id.btnCreate:
                createUserAccount();
                break;

            case R.id.btnBack:
                SignInActivity.this.finish();
                SignInActivity.this.setResult(RESULT_CLOSE_APPLICATION);
                break;


            /*case R.id.btnSignOut:
                signUserOut();
                break;*/
        }
    }

    private boolean checkFormFields() {
        String email, password;

        email = etEmail.getText().toString();
        password = etPass.getText().toString();

        if (email.isEmpty()) {
            etEmail.setError("Email Required");
            return false;
        }
        if (password.isEmpty()){
            etPass.setError("Password Required");
            return false;
        }

        return true;
    }

   /* private void updateStatus() {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            tvStat.setText("Signed in: " + user.getEmail());
        }
        else {
            tvStat.setText("Signed Out");
        }
    }
*/
    private void updateStatus(String stat) {
        TextView tvStat = (TextView)findViewById(R.id.tvSignInStatus);
        if(tvStat!=null) {
            tvStat.setText(stat);
        }
    }

    private boolean isEmailVerified() {

        Log.d(TAG,"Email Verification started");
        boolean result=false;
        // Start verification email check
        // [START send_email_verification]
        final FirebaseUser user = mAuth.getCurrentUser();
        if(user.isEmailVerified())
        {
            Log.d(TAG,"Email Verification Successful!!");
            result=true;
        }else
        {
            Log.d(TAG,"Email Verification Failed!!");
        }

        Log.d(TAG,"Email Verification completed");
        return result;
        // [END send_email_verification]
    }

    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // TODO: sign the user in with email and password credentials
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    if(isEmailVerified()) {
                                        Toast.makeText(SignInActivity.this, "Sign in Successful", Toast.LENGTH_SHORT)
                                                .show();
                                    }else
                                    {

                                        Toast.makeText(SignInActivity.this, "User not verified!! Please verify through mail activation link!!", Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                }
                                else {
                                    Toast.makeText(SignInActivity.this, "Sign in failed", Toast.LENGTH_SHORT)
                                            .show();
                                    signInWait.setVisibility(View.GONE);
                                }

                                //updateStatus();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            updateStatus("Invalid password.");
                        }
                        else if (e instanceof FirebaseAuthInvalidUserException) {
                            updateStatus("No account with this email.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        SignInActivity.this.finish();
        SignInActivity.this.setResult(RESULT_CLOSE_APPLICATION);
        //updateStatus();
    }

    private void createUserAccount() {
        Intent registerIntent = new Intent(SignInActivity.this, RegisterActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        //SignInActivity.this.startActivity(registerIntent);
        SignInActivity.this.startActivityForResult(registerIntent,requestCode);

       /* if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();


        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "User created", Toast.LENGTH_SHORT)
                                            .show();
                                } else {
                                    Toast.makeText(SignInActivity.this, "Account creation failed", Toast.LENGTH_SHORT)
                                            .show();
                                }
                                Toast.makeText(SignInActivity.this, "Inside addOnCompleteListener", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, e.toString());
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            updateStatus("This email address is already in use.");
                        }
                        else {
                            updateStatus(e.getLocalizedMessage());
                        }
                        Toast.makeText(SignInActivity.this, "Inside addOnFailureListener", Toast.LENGTH_SHORT)
                                .show();
                    }
                });*/
    }



}

