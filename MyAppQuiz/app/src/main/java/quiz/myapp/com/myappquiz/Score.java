package quiz.myapp.com.myappquiz;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

/**
 * Created by venkatesh on 11/19/2017.
 */

public class Score extends AppCompatActivity implements View.OnClickListener{

    private TextView txtProgress,scoreView;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;
    public FirebaseUser user;
    private final int REQUEST_CODE=20,RESULT_CLOSE_APPLICATION=30;
    private final String TAG = "SCORE_ACT";
    private int pStatus=0,score;
    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_score);
        setContentView(R.layout.chart_score_activity);
        Intent intent = getIntent();
        score=Integer.parseInt(intent.getStringExtra("SCORE"));
        scoreView=(TextView)findViewById(R.id.scoreview);
        //scoreView.setText(scoreView.getText()+String.valueOf(score));
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (pStatus < score) {
                    pStatus += 1;

                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            progressBar.setProgress(pStatus);
                            scoreView.setText("Validatig Score "+pStatus + "%...");
                            if(pStatus==score)
                            {
                                scoreView.setText("Your score: "+String.valueOf(score));
                            }

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(40); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();


        //progressBar.setProgress(score);

       /* Bitmap score_frame = BitmapFactory.decodeResource(getResources(), R.mipmap.score);
        Bitmap newBitmap = Bitmap.createScaledBitmap(score_frame,dp2px(320),360,true);
                //(originalImage, width, height, filter);
        ImageView imageView = (ImageView) findViewById(R.id.iv);
        imageView.setImageBitmap(newBitmap);*/

        /*LineChart chart = (LineChart) findViewById(R.id.chart);*/


        //imageView.setImageResource(R.mipmap.score);
/*
        Picasso.with(imageView.getContext())
                .load("http://o.aolcdn.com/hss/storage/midas/19dcdabec46a02182add1b78b897392/202720874/google-pixel-c-1200.jpg")
                .resize(dp2px(320), 0)
                .into(imageView);
*/





        mAuth = FirebaseAuth.getInstance();

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

    public int dp2px(int dp) {
        WindowManager wm = (WindowManager) this.getBaseContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        display.getMetrics(displaymetrics);
        return (int) (dp * displaymetrics.density + 0.5f);
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
                Score.this.finish();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            // action with ID action_settings was selected
//            case R.id.action_settings:
//                Log.d("MAIN_ACT", "Settings Selected");

//                break;

            case android.R.id.home:
                signUserOut();
                Score.this.finish();
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
        Score.this.setResult(RESULT_CLOSE_APPLICATION);
    }
}
