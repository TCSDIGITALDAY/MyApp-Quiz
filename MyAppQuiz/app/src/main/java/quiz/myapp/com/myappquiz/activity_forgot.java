package quiz.myapp.com.myappquiz;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class activity_forgot extends AppCompatActivity implements View.OnClickListener{

    private Button btnForgotPassword;
    private EditText txtEmail;
    private ProgressBar forgotWait;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final String TAG = "RESET_ACT";
    private final int REQUEST_CODE=20,RESULT_CLOSE_APPLICATION=30;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot);
        findViewById(R.id.btnForgotPassword).setOnClickListener(this);
        //findViewById(R.id.btnSignOut).setOnClickListener(this);
        txtEmail = (EditText)findViewById(R.id.forgotEmailAddr);
        forgotWait = (ProgressBar) findViewById(R.id.PBForgotPassword);
        btnForgotPassword=(Button) findViewById(R.id.btnForgotPassword);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "Signed in: " + user.getUid());

                    /*Toast.makeText(activity_forgot.this, "Signed in "+user.getUid(), Toast.LENGTH_SHORT)
                            .show();*/
                } else {
                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                    /*Toast.makeText(activity_forgot.this, "Signed Out", Toast.LENGTH_SHORT)
                            .show();*/
                }
            }
        };

       /* Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnForgotPassword:
                forgotWait.setVisibility(View.VISIBLE);
                forgotPassword();
                forgotWait.setVisibility(View.GONE);
                closeAll();
                break;
            /*case R.id.btnSignOut:
                signUserOut();
                break;*/
        }
    }

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

    private void forgotPassword() {

        String email = txtEmail.getText().toString();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email Sent");
                                    Toast.makeText(activity_forgot.this, "An email has beenn sent you!! Please reset through reset link!!", Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
    }

    private void  closeAll()
    {
        activity_forgot.this.setResult(RESULT_CLOSE_APPLICATION);
    }

}
