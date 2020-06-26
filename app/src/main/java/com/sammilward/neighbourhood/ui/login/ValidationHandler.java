package com.sammilward.neighbourhood.ui.login;
import android.view.View;
import android.widget.EditText;

public class ValidationHandler {

    private final int passwordMinimumLength = 6;
    private final String passwordLongerThanMinimumRegex2 = String.format("^.{%d,}$", passwordMinimumLength);

    public boolean isNameValid(String displayName)
    {
        String displayNameRegex = "^[a-zA-Z., ]*$";
        if(displayName.isEmpty()) return false;
        if(displayName.equals("")) return false;
        if(!displayName.matches(displayNameRegex)) return false;
        return true;
    }

    public boolean isEventNameValid(String eventName)
    {
        String eventNameRegex = "^[a-zA-Z0-9., ]*$";
        if(eventName.isEmpty()) return false;
        if(eventName.equals("")) return false;
        if(!eventName.matches(eventNameRegex)) return false;
        return true;
    }

    public boolean isEmailValid(String email)
    {
        String emailRegex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(emailRegex);
    }

    public boolean isPostcodeValid(String postcode)
    {
        String postcodeRegex = "(GIR 0AA)|((([A-Z-[QVX]][0-9][0-9]?)|(([A-Z-[QVX]][A-Z-[IJZ]][0-9][0-9]?)|(([A-Z-[QVX]][0-9][A-HJKSTUW])|([A-Z-[QVX]][A-Z-[IJZ]][0-9][ABEHMNPRVWXY]))))\\s?[0-9][A-Z-[CIKMOV]]{2})";
        return postcode.matches(postcodeRegex);
    }

    public boolean isPasswordValid(String password)
    {
        String passwordContainsDigitRegex = ".*\\d.*";
        boolean containsDigit = password.matches(passwordContainsDigitRegex);
        boolean longerThanMinimum = password.matches(passwordLongerThanMinimumRegex2);
        return  containsDigit == longerThanMinimum && containsDigit == true;
    }

    public boolean doPasswordsMatch(String password, String passwordConfirm)
    {
        return password.matches(passwordConfirm);
    }

    public void displayValidationError(View view, String message)
    {
        EditText validationView = (EditText) view;
        validationView.setError(message);
    }

    public void removeValidationError(View view)
    {
        EditText validationView = (EditText) view;
        validationView.setError(null);
    }
}
