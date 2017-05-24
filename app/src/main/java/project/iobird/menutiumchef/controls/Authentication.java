package project.iobird.menutiumchef.controls;

/**
 * Created by iobird-oussama on 27/01/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.widget.EditText;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserInfo;

import project.iobird.menutiumchef.LoginActivity;
import project.iobird.menutiumchef.R;

public class Authentication {

    static FirebaseAuth mFirebaseAuth;

    //This Method used to verify if there is Internet connection...
    public static boolean isDataConnected(final Activity activity){
        ConnectivityManager connectMan = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectMan.getActiveNetworkInfo();
        if(activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting()){
            return true;
        }else{
            Utils.showToast(activity, R.string.err_no_internet, Constants.TIME_TWO_SECONDS, R.color.white);
            return false;
        }
    }

    //createAccount : This method use given email & password and tries to create
    //                new user into Firebase.
    public static void createAccount (String email, String pass, final Activity activity) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e){
                                Utils.showToast(activity, R.string.err_mail_already_used, Constants.TIME_TWO_SECONDS, 0);
                            } catch (Exception e) {
                                Utils.showToast(activity, R.string.err_signing, Constants.TIME_TWO_SECONDS, 0);
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //emailAuth : method used to connect a user to Firebase
    //            using given email and password
    public static void emailAuth(String mail, String pass, final Activity activity) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signInWithEmailAndPassword(mail, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Utils.showToast(activity, R.string.error_invalid_account, Constants.TIME_TWO_SECONDS, 0);
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //googleAuth : method that uses an IdToken, get Google Credential from it
    //             then use that Credential to connect to Firebase
    public static void googleAuth(final Activity activity, GoogleSignInAccount acct) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                Utils.showToast(activity, R.string.err_mail_already_used, Constants.TIME_TWO_SECONDS, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    //
    public static void resetPassword(final Activity activity, String emailOfForgottenPassword) {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.sendPasswordResetEmail(emailOfForgottenPassword.trim())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Utils.showToast(activity, R.string.email_reset_password, Constants.TIME_TWO_SECONDS, 0);
                        } else {
                            try {
                                throw task.getException();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Utils.showToast(activity, R.string.error_invalid_email, Constants.TIME_TWO_SECONDS, 0);
                            }
                        }
                    }
                });
    }

    //logout : method used to logout from Firebase Authentication
    public static void logout() {
        for (UserInfo user: mFirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if(user.getProviderId().equals("firebase")){
                mFirebaseAuth.getInstance().signOut();
            }else if(user.getProviderId().equals("google.com")) {
                try{
                    if(null != LoginActivity.mGoogleApiClient)
                        LoginActivity.mGoogleApiClient.disconnect();
                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    //getCurrentUser : method used to get the current user as a FirebaseUser variable
    public static FirebaseUser getCurrentUser(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        return mFirebaseAuth.getCurrentUser();
    }

    public static String getUsername() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        if(mFirebaseAuth.getCurrentUser()!=null) {
            if(mFirebaseAuth.getCurrentUser().getDisplayName()!=null && !mFirebaseAuth.getCurrentUser().getDisplayName().isEmpty()) {
                return mFirebaseAuth.getCurrentUser().getDisplayName();
            }else{
                return mFirebaseAuth.getCurrentUser().getEmail();
            }
        }else{
            return " - ";
        }
    }

}

