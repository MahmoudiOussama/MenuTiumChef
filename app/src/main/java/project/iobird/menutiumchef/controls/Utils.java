package project.iobird.menutiumchef.controls;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.text.DateFormat;
import java.util.Date;

import project.iobird.menutiumchef.R;

/**
 * Created by ioBirdOussama on 09/04/2017.
 */

public class Utils {

    private Utils() {}

    /**
     * Generate top layer progress indicator.
     *
     * @param context    activity context
     * @param cancelable can be progress layer canceled
     * @return dialog
     */
    public static ProgressDialog generateProgressDialog(Context context, boolean cancelable) {
        ProgressDialog progressDialog = new ProgressDialog(context, R.style.NewDialog);
        progressDialog.setMessage(context.getString(R.string.loading));
        progressDialog.setCancelable(cancelable);
        return progressDialog;
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public static boolean checkPlayServices(Activity activity) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(activity, resultCode, 9000)
                        .show();
            }
            return false;
        }
        return true;
    }

    /**
     * Check if textInputLayout contains editText view. If so, then set text value to the view.
     *
     * @param textInputLayout wrapper for the editText view where the text value should be set.
     * @param text            text value to display.
     */
    public static void setTextToInputLayout(TextInputLayout textInputLayout, String text) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            textInputLayout.getEditText().setText(text);
        }
    }

    /**
     * Check if textInputLayout contains editText view. If so, then return text value of the view.
     *
     * @param textInputLayout wrapper for the editText view.
     * @return text value of the editText view.
     */
    public static String getTextFromInputLayout(TextInputLayout textInputLayout) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            return textInputLayout.getEditText().getText().toString();
        } else {
            return null;
        }
    }


    /**
     * Method checks if text input layout exist and contains some value.
     * If layout is empty, then show error value under the textInputLayout.
     *
     * @param textInputLayout textInputFiled for check.
     * @param errorValue      value displayed when ext input is empty.
     * @return true if everything ok.
     */
    public static boolean checkTextInputLayoutValueRequirement(TextInputLayout textInputLayout, String errorValue) {
        if (textInputLayout != null && textInputLayout.getEditText() != null) {
            String text = Utils.getTextFromInputLayout(textInputLayout);
            if (text == null || text.isEmpty()) {
                textInputLayout.setErrorEnabled(true);
                textInputLayout.setError(errorValue);
                return false;
            } else {
                textInputLayout.setErrorEnabled(false);
                return true;
            }
        } else {
            return false;
        }
    }

    /** Get date as TimeStamp (long) and parse it to readable date.*/
    public static String parseDateToDisplay(long createdAt) {
        return DateFormat.getDateTimeInstance(DateFormat.MEDIUM ,DateFormat.SHORT).format(new Date(createdAt));
    }

    /* Show custom Toast Message, message displayed from String object*/
    public static void showToast(Activity activity, String message, int duration, int color) {
        if (activity == null) {
            return;
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom, (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.toast_text);
        if (((Integer) color) != null && color>0) {
            text.setTextColor(color);
        }
        ImageView iv = (ImageView) layout.findViewById(R.id.toast_image);

        final Toast toast = new Toast(activity);
        text.setText(message);
        if (R.mipmap.icon != 0) {
            iv.setImageResource(R.mipmap.icon);
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }

        toast.setView(layout);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    /* Show custom Toast Message, message displayed from strings file*/
    public static void showToast(Activity activity, int message, int duration, int color) {
        if (activity == null) {
            return;
        }
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_custom, (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.toast_text);
        if (color!=0) {
            text.setTextColor(color);
        }
        ImageView iv = (ImageView) layout.findViewById(R.id.toast_image);

        final Toast toast = new Toast(activity);
        text.setText(activity.getResources().getString(message));
        if (R.mipmap.icon != 0) {
            iv.setImageResource(R.mipmap.icon);
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.GONE);
        }

        toast.setView(layout);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}
