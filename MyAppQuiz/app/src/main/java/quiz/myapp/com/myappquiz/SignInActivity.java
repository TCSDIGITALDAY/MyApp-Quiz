package quiz.myapp.com.myappquiz;

/**
 * Created by venkatesh on 10/2/2017.
 */


import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class SignInActivity extends AppCompatActivity
        implements View.OnClickListener {
    private final String TAG = "SIGNIN_ACT";
    private final int REQUEST_CODE = 20, RESULT_CLOSE_APPLICATION = 30;
    //private int requestCode = 10;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences mPrefs;
    private EditText etPass;
    private EditText etEmail;
    private TextView etForgot;
    private Switch swRemember;
    public ProgressBar signInWait;
    private int requestCode = 10;

    //For FingerPrint
    private static final String DIALOG_FRAGMENT_TAG = "fingerprintFragment";
    static final String DEFAULT_KEY_NAME = "Quiz_Key";
    private Cipher mCipher;
    private FingerprintManager fingerprintManager;

    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private SharedPreferences mSharedPreferences;

    private Button mEnrollFingerPrint;
    private byte[] mEncryptionIv;


    /**
     * Standard Activity lifecycle methods
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // Set up click handlers and view item references
        findViewById(R.id.btnCreate).setOnClickListener(this);
        findViewById(R.id.btnSignIn).setOnClickListener(this);
        //findViewById(R.id.btnSignOut).setOnClickListener(this);

        etEmail = (EditText) findViewById(R.id.etEmailAddr);
        etPass = (EditText) findViewById(R.id.etPassword);
        signInWait = (ProgressBar) findViewById(R.id.PBsignIn);
        etForgot = (TextView) findViewById(R.id.tvForgotPass);
        swRemember = (Switch) findViewById(R.id.swRemember);
        etForgot.setOnClickListener(this);
        swRemember.setOnClickListener(this);


        //for fingerprint
        mEnrollFingerPrint = (Button) findViewById(R.id.enroll_finger_btn);
        mEnrollFingerPrint.setOnClickListener(this);
        fingerprintManager = getSystemService(FingerprintManager.class);
        mPrefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);


        // TODO: Get a reference to the Firebase auth object
        mAuth = FirebaseAuth.getInstance();

        // TODO: Attach a new AuthListener to detect sign in and out
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    if (! user.isEmailVerified()){
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "Email sent.");
                                }
                            }
                        });

                        Toast.makeText(SignInActivity.this, "Please verify you email id.", Toast.LENGTH_SHORT)
                                .show();
                        mAuth.signOut();
                        return;
                    }

                    Log.d(TAG, "Signed in: " + user.getUid());

                    Toast.makeText(SignInActivity.this, "You are Signed in ", Toast.LENGTH_SHORT)
                            .show();

                    Intent beaconIntent = new Intent(SignInActivity.this, MainActivity.class);
                    //myIntent.putExtra("key", value); //Optional parameters
                    beaconIntent.putExtra("USER_UID", mAuth.getCurrentUser().getUid());
                    beaconIntent.putExtra("USER_EMAIL", mAuth.getCurrentUser().getEmail());
                    signInWait.setVisibility(View.GONE);
                    SignInActivity.this.startActivity(beaconIntent);


//                    SignInActivity.this.startActivityForResult(beaconIntent, REQUEST_CODE);


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

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mPrefs.getBoolean("FIRST_RUN", true)) {
            Log.d("SIGN_ACT", "Subscribing for Events");
            FirebaseMessaging.getInstance().subscribeToTopic("EVENTS");
            mPrefs.edit().putBoolean("FIRST_RUN", false).commit();


        }
        String email = mPrefs.getString("EMAIL_ID","");
        if (! email.isEmpty()) {
            etEmail.setText(email);
        }
        if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints() ) {

            if (!mPrefs.getString("ENPS","").equals("") & !mPrefs.getString("ENIV", "").equals("") &! email.isEmpty()) {
                mEnrollFingerPrint.setVisibility(View.GONE);
                authFingerprint();
            } else {
                mEnrollFingerPrint.setVisibility(View.VISIBLE);
            }


        } else {
            mEnrollFingerPrint.setVisibility(View.GONE);
        }
        Log.d(TAG, "ON RESUME "+email);
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == REQUEST_CODE) {
            Log.d(TAG, "Request Code:" + String.valueOf(requestCode));
            if (resultCode == RESULT_CLOSE_APPLICATION) {
                Log.d(TAG, "Result Code:" + String.valueOf(resultCode));
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
        super.onStart();
        // TODO: add the AuthListener
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
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
                rememberUser();
                signUserIn();
                break;

            case R.id.btnCreate:
                createUserAccount();
                break;
            case R.id.tvForgotPass:
//                Toast.makeText(SignInActivity.this, "forgot clicked", Toast.LENGTH_SHORT).show();
                forgotPassword();
                break;
//            case R.id.swRemember:
////                Toast.makeText(SignInActivity.this, "remember clicked", Toast.LENGTH_SHORT).show();
//                rememberUser();
//                break;
            /*case R.id.btnSignOut:
                signUserOut();
                break;*/
            case R.id.enroll_finger_btn:
                enrollFingerprint();
                break;
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
        if (password.isEmpty()) {
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
        TextView tvStat = (TextView) findViewById(R.id.tvSignInStatus);
        if (tvStat != null) {
            tvStat.setText(stat);
        }
    }


    private void signUserIn() {
        if (!checkFormFields())
            return;

        String email = etEmail.getText().toString();
        String password = etPass.getText().toString();

        // TODO: sign the user in with email and password credentials
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(SignInActivity.this, "Sign in Successful", Toast.LENGTH_SHORT)
                                            .show();

                                }
                            }
                        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            mPrefs.edit().putString("ENPS", "").apply();
                            mPrefs.edit().putString("EMAIL_ID","").apply();
                            Log.e(TAG, e.toString());
                            updateStatus("Invalid password.");
                        } else if (e instanceof FirebaseAuthInvalidUserException) {
                            mPrefs.edit().putString("EMAIL_ID","").apply();
                            Log.e(TAG, e.toString());
                            updateStatus("No account with this email.");
                        } else {
                            Log.e(TAG, e.toString());
                            updateStatus(e.getLocalizedMessage());
                        }
                    }
                });
    }

    private void signUserOut() {
        // TODO: sign the user out
        mAuth.signOut();
        //updateStatus();
    }

    private void createUserAccount() {
        Intent registerIntent = new Intent(SignInActivity.this, RegisterActivity.class);
        //myIntent.putExtra("key", value); //Optional parameters
        //SignInActivity.this.startActivity(registerIntent);
        SignInActivity.this.startActivityForResult(registerIntent, requestCode);

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
    private void forgotPassword(){
        String emailAddress = etEmail.getText().toString();
        if (emailAddress.isEmpty()){
            Toast.makeText(SignInActivity.this, "Please enter the email id", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mAuth.sendPasswordResetEmail(emailAddress)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private void rememberUser(){
        String emailAddress = etEmail.getText().toString();
        if (emailAddress.isEmpty()){
            Toast.makeText(SignInActivity.this, "Please enter the email id", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        if (swRemember.isChecked()) {
            mPrefs.edit().putString("EMAIL_ID", emailAddress).commit();
//            Toast.makeText(SignInActivity.this, "email id remembered", Toast.LENGTH_SHORT).show();
        }
    }




//for fingerprint
    public void initKeyStore() {
        try {
                mKeyStore = KeyStore.getInstance("AndroidKeyStore");
                } catch (KeyStoreException e) {
                throw new RuntimeException("Failed to get an instance of KeyStore", e);
                }
                try {
                mKeyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                throw new RuntimeException("Failed to get an instance of KeyGenerator", e);
                }

                try {
                mCipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                + KeyProperties.BLOCK_MODE_CBC + "/"
                + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
                throw new RuntimeException("Failed to get an instance of Cipher", e);
                }

    }

    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        // The enrolling flow for fingerprint. This is where you ask the user to set up fingerprint
        // for your flow. Use of keys is necessary if you need to know if the set of
        // enrolled fingerprints has changed.
        try {
            mKeyStore.load(null);
            // Set the alias of the entry in Android KeyStore where the key will appear
            // and the constrains (purposes) in the constructor of the Builder

            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    // Require the user to authenticate with a fingerprint to authorize every use
                    // of the key
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            // This is a workaround to avoid crashes on devices whose API level is < 24
            // because KeyGenParameterSpec.Builder#setInvalidatedByBiometricEnrollment is only
            // visible on API level +24.
            // Ideally there should be a compat library for KeyGenParameterSpec.Builder but
            // which isn't available yet.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }
            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean initCipher(String keyName,int mode  ) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);

            if (mode==Cipher.ENCRYPT_MODE) {
                mCipher.init(mode, key);
                mEncryptionIv = mCipher.getIV();
            } else {
                mEncryptionIv = Base64.decode(mPrefs.getString("ENIV", ""),0);
                mCipher.init(mode, key,new IvParameterSpec(mEncryptionIv));
            }

            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to init Cipher"+e.getLocalizedMessage(), e);
        }
    }
    public void showFingerPrintFragment(String mKeyName, String mode) {

        // Set up the crypto object for later. The object will be authenticated by use
        // of the fingerprint.
        if (mode.equals("NEW")) {

            if (initCipher(mKeyName, Cipher.ENCRYPT_MODE)) {
                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                fragment.setStage(
                        FingerprintAuthenticationDialogFragment.Stage.NEW_FINGERPRINT_ENROLLED);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        } else if (mode.equals("AUTH")) {
            if (initCipher(mKeyName, Cipher.DECRYPT_MODE)) {
                FingerprintAuthenticationDialogFragment fragment
                        = new FingerprintAuthenticationDialogFragment();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mCipher));
                fragment.setStage(
                        FingerprintAuthenticationDialogFragment.Stage.FINGERPRINT);
                fragment.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }
    }


    public void enrollFingerprint (){
        if (!checkFormFields())
            return;
        initKeyStore();
        createKey(DEFAULT_KEY_NAME,true);
        showFingerPrintFragment(DEFAULT_KEY_NAME,"NEW");

    }

    public void authFingerprint() {
        initKeyStore();
        showFingerPrintFragment(DEFAULT_KEY_NAME,"AUTH");
    }

    /**
     * Tries to encrypt some data with the generated key in {@link #createKey} which is
     * only works if the user has just authenticated via fingerprint.
     */
    public void tryEncrypt(Cipher cipher) {
        try {
            byte[] encrypted = cipher.doFinal(etPass.getText().toString().getBytes());
            mPrefs.edit().putString("ENPS", Base64.encodeToString(encrypted, 0 /* flags */)).apply();
            mPrefs.edit().putString("ENIV", Base64.encodeToString(mEncryptionIv,0)).apply();
            mPrefs.edit().putString("EMAIL_ID",etEmail.getText().toString()).apply();
            signUserIn();

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(this, "Finger print Authentication failed. Please use password to log in. "
                    , Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to encrypt the data with the generated key." + e.getMessage());
        }
    }

    public void tryDecrypt(Cipher cipher) {
        try {
            String ps = mPrefs.getString("ENPS","");

            if(ps.equals(""))
                return;
            byte[] decrypted = cipher.doFinal(Base64.decode(ps,0));
            String email = mPrefs.getString("EMAIL_ID","");
            if (! email.isEmpty()) {
                etEmail.setText(email);
            }
            etPass.setText(new String(decrypted));
            Log.e(TAG, "Decrypted Pass." + etPass.getText().toString());
            signUserIn();

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            Toast.makeText(this, "Finger print Authentication failed. Please use password to log in. "
                    , Toast.LENGTH_LONG).show();
            Log.e(TAG, "Failed to decrypt the data with the generated key." + e.getMessage());
        }
    }

}