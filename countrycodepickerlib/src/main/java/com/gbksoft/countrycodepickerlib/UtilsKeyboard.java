package com.gbksoft.countrycodepickerlib;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UtilsKeyboard {

    public static void hideKeyboard(Context context, View view){
        if (view != null && context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    public static void showKeyboard(Context context) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View focusedView = null;
            if (context instanceof Activity) {
                focusedView = ((Activity) context).getCurrentFocus();
            }
            if (imm != null) {
                imm.showSoftInput(focusedView, InputMethodManager.SHOW_FORCED);
            }
        }
    }
}
