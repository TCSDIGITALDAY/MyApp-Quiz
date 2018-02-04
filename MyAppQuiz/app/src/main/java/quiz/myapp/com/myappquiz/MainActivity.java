package quiz.myapp.com.myappquiz;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private RecyclerView mRecyclerView;
    //private RecyclerView.Adapter mAdapter;
    private ListViewAdapter mAdapter;
    ImageView mnoquizErrorImageView;
    //private MyRecyclerViewAdapter customAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DividerItemDecoration mDividerItemDecoration;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ArrayList results;
    private ProgressBar progressBar;
    private ArrayList<Integer> pos;
    private FirebaseAuth mAuth;
    private Map<String, Object> quizResult = new HashMap<>();
    DatabaseReference mDatabase, mQuiz, mQuizItem, mUserData, mScoreData;
    public ProgressBar pbloadWait;
    // String mUserName,strMsg;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private final int REQUEST_CODE = 20, RESULT_CLOSE_APPLICATION = 30;
    public Button btnSubmit, btnBack;


    public FirebaseUser user;
    Users muser;
    int score = 0, answer, question, prevQuestion = 0, prePosition = 0;

    String userid, firstClick = "Y", activeQuiz = "", reviewFlag = "",noActiveQuizFlag="N";
    public ArrayList<QuizContainer> quizData = null;
    private final String TAG = "MAIN_ACT";
    private  int TOTAL_QUESTIONS = 4;
    private long pStatus = 0, progress = 0;
    private double currentPercent = 0.00, incrementPercent = 0.00;
    private int numofQuestions = 0;
    Map<Integer, Integer> resultSet = null;
    public ArrayList<String> questionsList = new ArrayList<>();
    public ArrayList<String> checkList=new ArrayList<>();
    private Handler handler = new Handler();
    public ValueEventListener quiztListener, quizItemListener, userListener,scoreListener;
    public HashMap<Integer,Integer> questionLookup=new HashMap<>();

    int currentQuestion = 0;
    //public String[] agendaDataset={"Digital day Kickoff - Saikat Mukherjee, Client Partner","Keynote speech by Girija Shankar - Head, Digital Enterprise for Healthcare","Digital Journey and Road ahead - Srivatsan Srinivasan, BRM"," Digital Talent development - Sangeeth Manukonda, EM","Blockchain - Vinodh Ramadoss, EM","Awards for Digital team","Conclusion - Srivatsan Srinivasan"};
    public String[] timeDataset = {"5 minutes", "30 minutes", "15 minutes", " 15 minutes", "20 minutes", "15 minutes", "5 minutes"};
    public String[] agendaDataset = {"Select the operating system which is NOT supported by Selenium IDE.", "Unix", "Linux", "Windows", "Solaris"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       /* incrementPercent = (double) (100 / TOTAL_QUESTIONS);
        Log.d(TAG, "Increment Pecent:" + incrementPercent);*/
        resultSet = new HashMap<Integer, Integer>();
        Intent intent = getIntent();
        userid = intent.getStringExtra("USER_UID");
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        pbloadWait = (ProgressBar) findViewById(R.id.PBloadQuestion);
        pbloadWait.setVisibility(View.VISIBLE);
        //mnoquizErrorImageView=(ImageView)findViewById(R.id.noquizErrorImageView) ;
        //mnoquizErrorImageView.setVisibility(View.INVISIBLE);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        progressBar = (ProgressBar) findViewById(R.id.statusBar);
        mDividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL);
        mRecyclerView.addItemDecoration(mDividerItemDecoration);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mQuizItem = mDatabase.child("quiz");
        userListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Inside User Listener:" + dataSnapshot.hasChildren());
                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    //quizResult.put(dataSnapshot.getKey(),dataSnapshot.getValue());
                    quizResult = (HashMap) dataSnapshot.getValue();

                    Log.d(TAG, "User Listener DAta:" + dataSnapshot.getKey() + ":" + dataSnapshot.getValue());
                    Log.d(TAG, "Quiz Result Data:" + quizResult);
                }
                setQuizItemListener();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }

        };

        //Get Current Quiz
        quizItemListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Inside Quiz ItemListener:" + dataSnapshot.hasChildren());
                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String tmpQuestion = jobSnapshot.getKey();
                    Log.d(TAG, "Inside Quiz ItemListener:" + tmpQuestion);
                    HashMap<String, Object> items = (HashMap) jobSnapshot.getValue();
                    for (Map.Entry<String, Object> pair : items.entrySet()) {
                        Log.d(TAG, "Entry:" + pair.getKey());
                        if (pair.getKey().equalsIgnoreCase("Status")) {
                            String keyItem = String.valueOf(pair.getValue());
                            Log.d(TAG, "Item:" + keyItem);
                            Log.d(TAG, "ItemLisener:" + tmpQuestion);
                            if (keyItem.equalsIgnoreCase("Active")) {

                                activeQuiz = tmpQuestion;

                                Log.d(TAG, "Inside Active:" + tmpQuestion);
                                mQuiz = mDatabase.child("quiz").child(activeQuiz).child("Questions");
                                setQuizListener();
                            }
                        }
                    }
                }
                if(activeQuiz.equalsIgnoreCase(""))

                {
                    Log.d(TAG,"No New Quiz");
                    loadNoQuizErrorGraphics(findViewById(android.R.id.content),"No New Quiz");
                    /*Toast.makeText(MainActivity.this, "No New Quiz", Toast.LENGTH_SHORT)
                            .show();
                    onStop();*/
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }

        };


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                Log.d(TAG, "Inside onAuthStateChanged");
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in

                    Log.d(TAG, "Signed in: " + user.getUid());

                    mUserData = mDatabase.child("users").child(user.getUid());
                    setUserListener();

                    Toast.makeText(MainActivity.this, "You are Signed in ", Toast.LENGTH_SHORT)
                            .show();

                } else {

                    // User is signed out
                    Log.d(TAG, "Currently signed out");
                    Toast.makeText(MainActivity.this, "Please Sign in to Continue!!", Toast.LENGTH_SHORT)
                            .show();

                }

            }
        };
        Log.d(TAG, mAuth.toString());
        Log.d(TAG, "Calling Sign in");
        reqPermissionCheck();
        //signUserIn("venky88r@gmail.com","1qazZaq1");

        quizData = new ArrayList<>();
        Log.d(TAG, "User Id:" + userid);
        //mUserData = mDatabase.child("users").child(userid);
        //mScoreData= mDatabase.child("scorecard");


        //mQuiz = mDatabase.child("quiz").child("Quiz 1").child("Questions");
        //mQuiz = mDatabase.child("quiz").child("Quiz 1");
        //child("QuizContainer")
        /*Get List of Beacons from Firebase*/
        quiztListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Log.d(TAG, "onDataChange");
                Log.d(TAG, "Count:" + dataSnapshot.getChildrenCount());
                Log.d(TAG, "Has Child Exists:" + dataSnapshot.hasChildren());
//                Log.d(TAG, dataSnapshot.getChildren().toString());

                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    String tmpQuestion = jobSnapshot.getKey();
                    numofQuestions++;
                    Log.d(TAG, "value of tmpQuest: " + tmpQuestion);
                    Log.d(TAG, "value of numofQuestions: " + numofQuestions);
                    //HashMap<String, Object> allData = (HashMap<String, Object>) dataSnapshot.getValue();
                   /* String[] yourChildArray = allData.keySet().toArray(new String[0]);
                    Log.d(TAG,"Child Aray:"+yourChildArray[0]);*/
                    //String[] yourChildArray = allData.keySet().toArray(new String[0]);
                    //Map<String,Boolean> resultDict=(Map)jobSnapshot.child(tmpQuestion).getValue();
                    Map<String, Boolean> resultDict = (Map) jobSnapshot.getValue();
                    Questions tmpDict = new Questions(tmpQuestion, resultDict);
                    QuizContainer quizItem = tmpDict.QuestionWrapper(numofQuestions);

                    //td.put(jobSnapshot.getKey(), job);
                    Log.d(TAG, "Question from DB:" + quizItem.getQuestion());
                    Log.d(TAG, "Option1 from DB:" + quizItem.getOption1());
                    Log.d(TAG, "Option2 from DB:" + quizItem.getOption2());
                    Log.d(TAG, "Option3 from DB:" + quizItem.getOption3());
                    Log.d(TAG, "Option4 from DB:" + quizItem.getOption4());
                    String questionListFlag="N";
                    if(questionsList.size()>0) {

                        for (String item:questionsList)
                        {
                            if(quizItem.getQuestion().equalsIgnoreCase(item))
                            {
                                questionListFlag="Y";
                            }
                        }

                    }

                    if(questionListFlag.equalsIgnoreCase("N")) {
                        questionsList.add(quizItem.getQuestion());
                    }
                    quizData.add(quizItem);

                }

                Log.d(TAG,"Review Quiz Size:"+questionsList.size());
                TOTAL_QUESTIONS=questionsList.size();
                incrementPercent = (double) (100 / TOTAL_QUESTIONS);
                Log.d(TAG, "Increment Pecent:" + incrementPercent);
                for (QuizContainer record : quizData) {

                    Log.d(TAG, record.getQuestion() + ":" + record.getAnswer());
                }
                mAdapter = new ListViewAdapter(getDataSet());
                if (pbloadWait.isShown()) {
                    pbloadWait.setVisibility(View.GONE);
                }

                //currentQuestion = currentQuestion + 1;

            /*    ListViewAdapter.ListClickListener listener = (view, position) -> {
                    Toast.makeText(MainActivity.this, "Position " + position, Toast.LENGTH_SHORT).show();
                };*/
                mAdapter.setOnItemClickListener(new ListViewAdapter.ListClickListener() {
                    @Override
                    public void onItemClick(int position, View v) {
                        //Toast.makeText(MainActivity.this, "Answer:"+answer+",Position " + position, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onItemClick");
                        int posCtr=0;
                        //Check if the cal is from Review Screen. If review Screen, skip the regular loop
                        if (reviewFlag.equalsIgnoreCase("Y")) {
                            /*for(String question:questionsList)
                            {
                                posCtr++;
                                if(question.equalsIgnoreCase(rev))
                                {
                                    position=posCtr;
                                }
                            }*/
                            Log.d(TAG, "Question no:"+questionLookup.get(position));
                            //refreshListView(position);
                            reviewFlag = "N";
                            checkList.remove(position-1);
                            refreshListView(questionLookup.get(position)-1);


                                                    } else {

                            if (firstClick.equalsIgnoreCase("N")) {
                                Log.d(TAG, "PrevQueston:" + prevQuestion + ";Question:" + question + "PrePosition:" + prePosition + ";Position:" + position);

                                if (prevQuestion == question && prePosition != position) {
                                    Log.d(TAG, "Inside If block");
                               /*pos=new ArrayList<Integer>();
                               pos.add(prePosition);
                               mAdapter.notifyItemChanged(position,pos);*/
                                    refreshListView(question - 1);
                                    Log.d(TAG, "After Refresh");
                                    //mAdapter.notifyItemChanged(position,pos);
                                    //refreshListView(question-1);

                                }
                            }
                            if (answer == position) {
                                //score++;
                                Log.d(TAG, "Question:" + question);
                                resultSet.put(question, 1);
                            } else {
                                Log.d(TAG, "Question:" + question);
                                resultSet.put(question, 0);
                            }

                            prevQuestion = question;
                            prePosition = position;
                            firstClick = "N";

                        }
                    }

                });

                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }
        };


        // specify an adapter (see also next example)
        //mAdapter = new MyRecyclerViewAdapter(getDataSet());


        // Set up click handlers and view item references
        findViewById(R.id.btnSubmit).setOnClickListener(this);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);

        findViewById(R.id.btnBack).setOnClickListener(this);

        btnBack = (Button) findViewById(R.id.btnBack);



      /*  mUserData.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                muser = dataSnapshot.getValue(Users.class);
                if (muser == null) {
                    //txtBeaconmsg.setVisibility(View.VISIBLE);
                    Log.d("MAIN_ACT", "On Data Change - no data found");
                } else {
                    Log.d("MAIN_ACT", "On Data Change - " + muser.toString());
//                mUserName = dataSnapshot.getKey();
                    //mUserName = muser.getFirstname();
                    mUserName = muser.getEmail();
                    Log.d(TAG, "Track:" + muser.getEmail());

                    DateFormat df = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        df = (DateFormat) new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    }

                    Log.d("MAIN_ACT", "On Data Change - " + mUserName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "Error:"+databaseError);
            }
        });*/
    }

    public void setQuizListener() {
        Log.d(TAG, "Inside Set Quiz Listener");
       /* String firstTimeEntry = "Y";
        for (Map.Entry<String, Object> pair : quizResult.entrySet()) {
            if (pair.getKey().equalsIgnoreCase("QuizName")) {
                firstTimeEntry = "N";
                if (activeQuiz.equalsIgnoreCase(String.valueOf(pair.getValue()))) {
                    Toast.makeText(MainActivity.this, "No New Quiz", Toast.LENGTH_SHORT)
                            .show();
                    if (pbloadWait.isShown()) {
                        pbloadWait.setVisibility(View.GONE);
                    }
                    *//*btnSubmit.setVisibility(View.GONE);
                    btnBack.setVisibility(View.GONE);*//*
                    loadNoQuizErrorGraphics(findViewById(android.R.id.content),"You have already attended the quiz!!");
                    //signUserOut();
                } else {
                    mQuiz.addValueEventListener(quiztListener);
                }
            }
        }

        if (!firstTimeEntry.equalsIgnoreCase("N")) {*/
            mQuiz.addValueEventListener(quiztListener);
        //}

    }

    public void setUserListener() {
        Log.d(TAG, "Inside Set User Listener");
        mUserData.addValueEventListener(userListener);
    }

    public void setQuizItemListener() {
        mQuizItem.addValueEventListener(quizItemListener);
    }

    public void setScoreListener() {
        mScoreData.addValueEventListener(scoreListener);
    }


    private ArrayList<DataObject> getDataSet() {
        Log.d(TAG, "Increment Pecent:" + incrementPercent);
        currentPercent = currentPercent + incrementPercent;
        Log.d(TAG, "currentPercent:" + currentPercent);
        progress = round(currentPercent, 5);
        setProgressBarStatus();
        results = new ArrayList<DataObject>();
        agendaDataset[0] = quizData.get(currentQuestion).getQuestion();
        agendaDataset[1] = quizData.get(currentQuestion).getOption1();
        agendaDataset[2] = quizData.get(currentQuestion).getOption2();
        agendaDataset[3] = quizData.get(currentQuestion).getOption3();
        agendaDataset[4] = quizData.get(currentQuestion).getOption4();
        question = Integer.parseInt(quizData.get(currentQuestion).getQuestionNo());
        answer = Integer.parseInt(quizData.get(currentQuestion).getAnswer());
        for (int index = 0; index < agendaDataset.length; index++) {
            DataObject obj = new DataObject(agendaDataset[index],
                    timeDataset[index]);
            results.add(index, obj);
        }
        return results;
    }

    private void getReviewDataSet() {
        ArrayList<String> reviewList = new ArrayList<>();
        reviewFlag = "Y";
    int tmpCtr=0;
        Log.d(TAG,"Quiz Size:"+questionsList.size());
        for(String question:questionsList)
        {

            Log.d(TAG,"Question Number:"+tmpCtr+",Question:"+question);
            tmpCtr++;
        }
        Log.d(TAG, "Inside Refresh");
        int ctr = 0;

        Log.d(TAG, "ReviewList Size:" + reviewList.size() + "Total Questions:" + TOTAL_QUESTIONS + "Counter:" + ctr + "Questions Answered:" + questionsList.size());
        //while (ctr <= TOTAL_QUESTIONS-1) {
        for(String questionItem:questionsList)
        {
            ctr++;
            String entryFlag = "Y";
            for (Map.Entry<Integer, Integer> pair : resultSet.entrySet()) {

                Log.d(TAG, "Question Number:" + pair.getKey());
                if (ctr == pair.getKey()) {
                    entryFlag = "N";

                }

            }
            if (entryFlag.equalsIgnoreCase("Y")) {
                //reviewList.add(questionsList.get(ctr));
                reviewList.add(questionItem);
                questionLookup.put(reviewList.size(),ctr);
            }


           /*  ctr++;
             if(ctr<questionsList.size()) {
                 reviewList.add(questionsList.get(ctr));
             }*/

        }

        Log.d(TAG, "Questions to be reviewed");
        for (String record : reviewList) {
            Log.d(TAG, "Review:"+record);
        }
        checkList=reviewList;
        if (reviewList.size() > 0) {
            results.clear();
            DataObject hdrObj = new DataObject("UnAnswered Questions",
                    timeDataset[0]);
            results.add(0, hdrObj);
            for (int index = 1; index <= reviewList.size(); index++) {
                DataObject obj = new DataObject(reviewList.get(index - 1),
                        timeDataset[index]);
                results.add(index, obj);
            }
            mAdapter.notifyDataSetChanged();
        } else {
            btnSubmit.performClick();
            //Submit Screen
        }


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSubmit:
                Log.d(TAG, "Increment Pecent:" + incrementPercent);
                currentPercent = currentPercent + incrementPercent;
                Log.d(TAG, "currentPercent:" + currentPercent);
                progress = round(currentPercent, 5);
                setProgressBarStatus();
                if (currentQuestion < TOTAL_QUESTIONS - 1) {
                    //question=currentQuestion;
                    currentQuestion = currentQuestion + 1;

                    refreshListView(currentQuestion);

                    Log.d(TAG, "Question after Click:" + question);
                } else if (currentQuestion == TOTAL_QUESTIONS - 1) {
                    currentQuestion = currentQuestion + 1;
                    //Call Review at the end of the Quiz
                    Log.d(TAG,"currentQuestion == TOTAL_QUESTIONS - 1");
                    getReviewDataSet();
                    /*Toast.makeText(MainActivity.this, "You have reached last Question", Toast.LENGTH_SHORT)
                            .show();*/

/*
                    Toast.makeText(MainActivity.this, "Score"+finalScore, Toast.LENGTH_SHORT)
                            .show();
*/

                } else if (currentQuestion == TOTAL_QUESTIONS) {
                    //Evaluate Score after final submission
                    if(checkList.size()>0)
                    {
                        getReviewDataSet();
                        Log.d(TAG,"currentQuestion == TOTAL_QUESTIONS");
                    }else {
                        int finalScore = Evaluate();
                        Log.d("MAIN_ACT", "Final Score:" + finalScore);
                        writeScore(userid, String.valueOf(finalScore), getWeekdt());
                        Intent scoreIntent = new Intent(MainActivity.this, Score.class);
                        //myIntent.putExtra("key", value); //Optional parametersScore Update to Firebase Completed
                        //scoreIntent.putExtra("SCORE", String.valueOf((finalScore * 100) / TOTAL_QUESTIONS));
                        scoreIntent.putExtra("SCORE", "100");

                        MainActivity.this.startActivityForResult(scoreIntent, REQUEST_CODE);
                    }
                } else {
                    //Call Review if all questions are visited atleast once

                    Log.d(TAG,"All questions are visited atleast once");
                    getReviewDataSet();
                    Toast.makeText(MainActivity.this, "You have reached last Question", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            case R.id.btnBack:
                Log.d(TAG, "Increment Pecent:" + incrementPercent);
                currentPercent = currentPercent - incrementPercent;
                Log.d(TAG, "currentPercent:" + currentPercent);
                progress = round(currentPercent, 5);
                setProgressBarStatus();
                Log.d(TAG, "Back Button Pressed");
                Log.d(TAG, "Current Question:" + currentQuestion);

                if (currentQuestion > 0) {
                    /*if(question<currentQuestion) {
                        currentQuestion = currentQuestion - 2;
                    }else
                    {
                */
                    currentQuestion = currentQuestion - 1;
                    //  }
                    Log.d(TAG, "Question Called for Refresh:" + currentQuestion);
                    refreshListView(currentQuestion);
                } else {
                    Toast.makeText(MainActivity.this, "You have reached first Question", Toast.LENGTH_SHORT)
                            .show();
                }
                break;


            default:
                break;
        }
    }

    private void refreshListView(int qno) {
        results.clear();
        Log.d(TAG, "Inside Refresh");
        //agendaDataset={"Select the component which is NOT part of Selenium suite.","Selenium IDE","Selenium RC","SeleniumGrid","Selenium Web"};
        agendaDataset[0] = quizData.get(qno).getQuestion();
        agendaDataset[1] = quizData.get(qno).getOption1();
        agendaDataset[2] = quizData.get(qno).getOption2();
        agendaDataset[3] = quizData.get(qno).getOption3();
        agendaDataset[4] = quizData.get(qno).getOption4();
        question = Integer.parseInt(quizData.get(qno).getQuestionNo());
//        answer = Integer.parseInt(quizData.get(qno).getAnswer());
        //question = ;
       /* agendaDataset[0]="Select the component which is NOT part of Selenium suite.";
        agendaDataset[1]="Selenium IDE";
        agendaDataset[2]="Selenium RC";
        agendaDataset[3]="Selenium Grid";
        agendaDataset[4]="Selenium Web";*/
        for (int i = 0; i < agendaDataset.length; i++) {
            DataObject obj = new DataObject(agendaDataset[i],
                    timeDataset[i]);
            results.add(i, obj);
        }
        mAdapter.notifyDataSetChanged();
       /* } else {

        }*/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    //advid=getAdvtersieId();
                }
                break;

            default:
                Log.d(TAG, "Not Shared");
                break;
        }
    }

    private void reqPermissionCheck() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
        } else {
            //TODO
            //advid=getAdvtersieId();
            Log.d(TAG, "Permission Granted");
        }

    }


    private void writeScore(String uid, String score, String weekdt) {


        quizResult.put("QuizName", activeQuiz);
        quizResult.put("Score", score);
        Scorecard objScoreCard = new Scorecard();
        //user.setDeviceid(advid);

        objScoreCard.setScore(score);
        //objScoreCard.setUserid(uid);
        //objScoreCard.setWeekdt(weekdt);
        //String userId = mDatabase.child("users").push().getKey();
        //mDatabase.child("scorecard").child(weekdt).child(uid).setValue(objScoreCard);
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/users/" + uid, quizResult);
        mDatabase.updateChildren(childUpdates);

        //findWinner();

        Log.e(TAG, "Score Update to Firebase Completed!!!");
        //progressDialog.dismiss();
    }

    public void findWinner()
    {
        mScoreData=mDatabase.child("users");
        scoreListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "Inside User Listener:" + dataSnapshot.hasChildren());
                for (DataSnapshot jobSnapshot : dataSnapshot.getChildren()) {
                    //quizResult.put(dataSnapshot.getKey(),dataSnapshot.getValue());
                   HashMap<String,Object> winnerDataset = (HashMap) dataSnapshot.getValue();
                    Log.d(TAG, "User Listener DAta:" + dataSnapshot.getKey() + ":" + dataSnapshot.getValue());
                    Log.d(TAG, "Quiz Result Data:" + winnerDataset);
                    Log.d(TAG,"Winner Score:"+winnerDataset.get("Score"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, databaseError.getMessage());
            }

        };

        setScoreListener();
    }

    public String getWeekdt() {
        DateFormat df = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            df = (DateFormat) new SimpleDateFormat("yyyy-MM-dd");
        }
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        String currentDate = df.format(calendar.getTimeInMillis());
        System.out.println(currentDate);
        Log.d(TAG, currentDate);
        return currentDate;
    }

    public int Evaluate() {
        Iterator it = resultSet.entrySet().iterator();
        int count = 0;
        while (it.hasNext()) {
            count++;
            Map.Entry pair = (Map.Entry) it.next();
         /*   Toast.makeText(MainActivity.this, "Answer:"+pair.getValue(), Toast.LENGTH_SHORT)
                    .show();*/
            Log.d(TAG, String.valueOf(pair.getValue()));
            score += Integer.parseInt(String.valueOf(pair.getValue()));
            Log.d("MAIN_ACT", "Question:" + String.valueOf(pair.getKey()) + ";Score:" + String.valueOf(pair.getValue()));
            //it.remove(); // avoids a ConcurrentModificationException
        }
        Log.d(TAG, "Count:" + count);
      /*  Toast.makeText(MainActivity.this, "Count:"+count, Toast.LENGTH_SHORT)
                .show();*/
        return score;
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

    long round(double i, int v) {
        return Math.round(i / v) * v;

    }

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
                Log.d("MAIN_ACT", "Logout Selected");
                //MainActivity.this.finish();
                signUserOut();
                NavUtils.navigateUpFromSameTask(this);
                return true;
            // action with ID action_settings was selected
//            case R.id.action_settings:
//                Log.d("MAIN_ACT", "Settings Selected");

//                break;

            case android.R.id.home:
                //MainActivity.this.finish();
                Log.d("MAIN_ACT", "Inside Home Button");
                signUserOut();
                NavUtils.navigateUpFromSameTask(this);
                MainActivity.this.setResult(RESULT_CLOSE_APPLICATION);
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setProgressBarStatus() {
        Log.d(TAG, "Set Progres Called:" + progress);

        new Thread(new Runnable() {

            @Override
            public void run() {
                pStatus = 0;
                // TODO Auto-generated method stub
                while (pStatus < progress) {
                    pStatus += 1;
                    Log.d(TAG, "Setting Progress:" + pStatus + "%");
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            progressBar.setProgress((int) pStatus);

                        }
                    });
                    try {
                        // Sleep for 200 milliseconds.
                        // Just to display the progress slowly
                        Thread.sleep(10); //thread will take approx 3 seconds to finish
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        MainActivity.this.setResult(RESULT_CLOSE_APPLICATION);
    }

    public void loadNoQuizErrorGraphics(View view,String message)
    {
        Log.d(TAG,"Load Error Graphics Callled");
        Intent noQuizIntent = new Intent(MainActivity.this, NoQuizFound.class);
        noQuizIntent.putExtra("ErrorMsg", message);
        MainActivity.this.startActivityForResult(noQuizIntent, REQUEST_CODE);
        //setContentView(R.layout.no_quiz_image_layout);
        //mnoquizErrorImageView.setVisibility(View.VISIBLE);

      /*  RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(200, 200);

        lp.addRule(RelativeLayout.CENTER_IN_PARENT); // A position in layout.
        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(lp);
// imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(R.mipmap.ic_warning_black);
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.mErrorMessage);
        layout.addView(imageView);*/
}

}
