package quiz.myapp.com.myappquiz;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by venkatesh on 1/9/2018.
 */

public class NoQuizFound extends AppCompatActivity implements View.OnClickListener{

    private TextView txtProgress,scoreView;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    public FirebaseUser user;
    private final int REQUEST_CODE=20,RESULT_CLOSE_APPLICATION=30;
    private final String TAG = "ERROR_ACT";
    private int pStatus=0;
    private String errorMSg="";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.no_quiz_image_layout);
        //setContentView(R.layout.activity_score);
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        errorMSg=intent.getStringExtra("ErrorMsg");
        Log.d(TAG,"Error MEssage:"+errorMSg);
        scoreView=(TextView)findViewById(R.id.noquizErrorTxtView);
        scoreView.setText(errorMSg);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "Inside onAuthStateChanged");
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Log.d(TAG, "Signed in: " + user.getUid());
/*
                    Toast.makeText(MainActivity.this, "You are Signed in ", Toast.LENGTH_SHORT)
                            .show();*/

                } else {

                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                    /*Toast.makeText(MainActivity.this, "Please Sign in to Continue!!", Toast.LENGTH_SHORT)
                            .show();
*/
                }

            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

    }

    @Override
    public void onClick(View v) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_logout:
                signUserOut();
                Log.d(TAG, "Logout Selected");
                NoQuizFound.this.finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            // action with ID action_settings was selected
//            case R.id.action_settings:
//                Log.d("MAIN_ACT", "Settings Selected");

//                break;

            case android.R.id.home:
                signUserOut();
                NoQuizFound.this.finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;


            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        NoQuizFound.this.setResult(RESULT_CLOSE_APPLICATION);
    }
}
