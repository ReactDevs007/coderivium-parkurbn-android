package com.cruxlab.parkurbn.tools;

import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.util.Patterns;

import com.cruxlab.parkurbn.R;

/**
 * Created by alla on 5/18/17.
 */

public class ValidatorUtils {

    private ValidatorUtils() {

    }

    public static boolean checkEmail(Context context, TextInputLayout etEmail, String email) {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError(context.getString(R.string.email_error));
            return false;
        }
        etEmail.setErrorEnabled(false);
        return true;
    }

    public static boolean checkPassword(Context context, TextInputLayout etPassword, String password) {
        if (password.isEmpty() || password.length() < 6) {
            etPassword.setError(context.getString(R.string.password_error));
            return false;
        }
        etPassword.setErrorEnabled(false);
        return true;
    }

    public static boolean checkLicensePlateNumber(Context context, TextInputLayout etPlate, String plate) {
        if (plate.isEmpty() || plate.length() < 4) {
            etPlate.setError(context.getString(R.string.plate_error));
            return false;
        } else if (plate.length() > 15) {
            etPlate.setError(context.getString(R.string.length_error));
            return false;
        }
        etPlate.setErrorEnabled(false);
        return true;
    }

    public static boolean checkNickname(Context context, TextInputLayout etNickname, String nickname) {
        if (nickname.length() > 30) {
            etNickname.setError(context.getString(R.string.length_error));
            return false;
        }
        etNickname.setErrorEnabled(false);
        return true;
    }

}
