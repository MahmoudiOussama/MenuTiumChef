package project.iobird.menutiumchef;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import project.iobird.menutiumchef.controls.Authentication;
import project.iobird.menutiumchef.controls.Constants;
import project.iobird.menutiumchef.controls.ReplaceFont;
import project.iobird.menutiumchef.controls.Utils;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private final int REQUEST_CODE = 1;
    private TextInputLayout mEmailView, mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    static int countGoogleInit=0;
    boolean signOrRegister = false;
    public static GoogleApiClient mGoogleApiClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        if (getIntent().getBooleanExtra(Constants.EXIT, false)) {
            finish();
        } else {
            //ReplaceFont.replaceDefaultFont(this, "MONOSPACE", "fonts/roboto_thin_italic.ttf");
            //ReplaceFont.replaceDefaultFont(this, "SERIF", "fonts/coffee_tea_regular.ttf");
            //ReplaceFont.replaceDefaultFont(this, "MONOSPACE", "fonts/roboto_medium.ttf");
            mAuth = FirebaseAuth.getInstance();
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                }
            };

            if (!Utils.checkPlayServices(this)) {
                Utils.showToast(this, R.string.common_google_play_services_install_text, Constants.TIME_TWO_SECONDS, 0);
            } else {
                // Set up the login form.
                mEmailView = (TextInputLayout) findViewById(R.id.email_input);
                mPasswordView = (TextInputLayout) findViewById(R.id.pass_input);


                findViewById(R.id.email_sign_in_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signOrRegister = false;
                        attemptLogin();
                    }
                });

                findViewById(R.id.email_register_button).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        signOrRegister = true;
                        attemptLogin();
                    }
                });

                initGoogle();
                //Listen to Google+ button click ....
                findViewById(R.id.google_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(Authentication.isDataConnected(LoginActivity.this)) {
                            googleIntent();
                        }
                    }
                });

                //Listen to reset password
                findViewById(R.id.reset_password).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findViewById(R.id.signing_layout).setVisibility(View.GONE);
                        findViewById(R.id.reset_layout).setVisibility(View.VISIBLE);
                    }
                });

                //Listen to cancel reset
                findViewById(R.id.cancel_reset).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        findViewById(R.id.signing_layout).setVisibility(View.VISIBLE);
                        findViewById(R.id.reset_layout).setVisibility(View.GONE);
                    }
                });

                //Listen to resetting confirmation
                findViewById(R.id.submit_email).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Authentication.isDataConnected(LoginActivity.this)){
                            if (TextUtils.isEmpty(Utils.getTextFromInputLayout(mEmailView))) {
                                mEmailView.setError(getString(R.string.error_field_required));
                            } else if (!isEmailValid(Utils.getTextFromInputLayout(mEmailView))) {
                                mEmailView.setError(getString(R.string.error_invalid_email));
                            } else {
                                Authentication.resetPassword(LoginActivity.this, Utils.getTextFromInputLayout(mEmailView));
                            }
                        }
                    }
                });

                mLoginFormView = findViewById(R.id.login_form);
                mProgressView = findViewById(R.id.login_progress);
            }
        }
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
    public void onResume(){
        super.onResume();
        if (getIntent().getBooleanExtra(Constants.EXIT, false)) {
            finish();
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = Utils.getTextFromInputLayout(mEmailView);
        String password = Utils.getTextFromInputLayout(mPasswordView);

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }


    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    // This Method used to initialise the GoogleSignInOptions and assign different requests
    private void initGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,
                        new GoogleApiClient.OnConnectionFailedListener() {
                            @Override
                            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                Utils.showToast(LoginActivity.this, R.string.err_signing, Constants.TIME_TWO_SECONDS, 0);
                            }
                        })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    // Display registered google accounts, user could select one for Signing
    private void googleIntent() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, REQUEST_CODE);
    }

    // This PreDefined mathod used to get result from launched Google Intent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                Authentication.googleAuth(LoginActivity.this, account);
            } else {
                try {
                    result.isSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Utils.showToast(LoginActivity.this, R.string.err_signing, Constants.TIME_TWO_SECONDS, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (signOrRegister) {
                Authentication.createAccount(mEmail, mPassword, LoginActivity.this);
            } else {
                Authentication.emailAuth(mEmail, mPassword, LoginActivity.this);
            }

            try {
                // Simulate network access.
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (!success) {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

