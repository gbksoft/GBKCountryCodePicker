package com.gbksoft.countrycodepickerlib;

import android.content.Context;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

//Reference https://stackoverflow.com/questions/32661363/using-phonenumberformattingtextwatcher-without-typing-country-calling-code to solve formatting issue
class InternationalPhoneTextWatcher implements TextWatcher {

    private static final String TAG = "Int'l Phone TextWatcher";
    private PhoneNumberUtil phoneNumberUtil;
    /**
     * Indicates the change was caused by ourselves.
     */
    private boolean mSelfChange = false;
    /**
     * Indicates the formatting has been stopped.
     */
    private boolean mStopFormatting;
    private AsYouTypeFormatter mFormatter;
    private String countryNameCode;
    private Editable lastFormatted = null;
    private CCPCountryGroup currentCountryGroup;

    //when country is changed, we update the number.
    //at this point this will avoid "stopFormatting"
    private boolean needUpdateForCountryChange = false;

    private boolean internationalOnly;

    /**
     * @param context
     * @param countryNameCode  ISO 3166-1 two-letter country code that indicates the country/region
     *                         where the phone number is being entered.
     * @param countryPhoneCode Phone code of country. https://countrycode.org/
     */
    InternationalPhoneTextWatcher(Context context, CCPCountryGroup currentCountryGroup, String countryNameCode, int countryPhoneCode) {
        this(context, currentCountryGroup, countryNameCode, countryPhoneCode, true);
    }

    /**
     * @param context
     * @param countryNameCode   ISO 3166-1 two-letter country code that indicates the country/region
     *                          where the phone number is being entered.
     * @param countryPhoneCode  Phone code of country. https://countrycode.org/
     * @param internationalOnly Specifies whether numbers should only be formatted if they are
     *                          international vs national
     */
    InternationalPhoneTextWatcher(Context context, CCPCountryGroup currentCountryGroup, String countryNameCode, int countryPhoneCode, boolean internationalOnly) {
        if (countryNameCode == null || countryNameCode.length() == 0) {
            throw new IllegalArgumentException();
        }
        this.currentCountryGroup = currentCountryGroup;
        phoneNumberUtil = PhoneNumberUtil.getInstance();
        updateCountry(countryNameCode, countryPhoneCode);
        this.internationalOnly = internationalOnly;
    }

    void updateCountry(String countryNameCode, int countryPhoneCode) {
        this.countryNameCode = countryNameCode;
        mFormatter = phoneNumberUtil.getAsYouTypeFormatter(String.valueOf(countryPhoneCode));
        mFormatter.clear();
        if (lastFormatted != null) {
            needUpdateForCountryChange = true;
            String onlyDigits = phoneNumberUtil.normalizeDigitsOnly(lastFormatted.toString());
            lastFormatted.replace(0, lastFormatted.length(), onlyDigits, 0, onlyDigits.length());
            needUpdateForCountryChange = false;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        if (mSelfChange || mStopFormatting) {
            return;
        }
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count) && !needUpdateForCountryChange) {
            stopFormatting();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mSelfChange || mStopFormatting) {
            return;
        }
        // If the user inserted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count)) {
            stopFormatting();
        }
    }

    @Override
    public synchronized void afterTextChanged(Editable s) {
        if (mStopFormatting) {
            // Restart the formatting when all texts were clear.
            mStopFormatting = !(s.length() == 0);
            return;
        }
        if (mSelfChange) {
            // Ignore the change caused by s.replace().
            return;
        }

        //calculate few things that will be helpful later
        int selectionEnd = Selection.getSelectionEnd(s);
        boolean isCursorAtEnd = (selectionEnd == s.length());

        //get formatted text for this number
        String formatted = reformat(s);

        //now calculate cursor position in formatted text
        int finalCursorPosition = 0;
        if (formatted.equals(s.toString())) {
            //means there is no change while formatting don't move cursor
            finalCursorPosition = selectionEnd;
        } else if (isCursorAtEnd) {
            //if cursor was already at the end, put it at the end.
            finalCursorPosition = formatted.length();
        } else {

            // if no earlier case matched, we will use "digitBeforeCursor" way to figure out the cursor position
            int digitsBeforeCursor = 0;
            for (int i = 0; i < s.length(); i++) {
                if (i >= selectionEnd) {
                    break;
                }
                if (PhoneNumberUtils.isNonSeparator(s.charAt(i))) {
                    digitsBeforeCursor++;
                }
            }

            //at this point we will have digitsBeforeCursor calculated.
            // now find this position in formatted text
            for (int i = 0, digitPassed = 0; i < formatted.length(); i++) {
                if (digitPassed == digitsBeforeCursor) {
                    finalCursorPosition = i;
                    break;
                }
                if (PhoneNumberUtils.isNonSeparator(formatted.charAt(i))) {
                    digitPassed++;
                }
            }
        }

        //if this ends right before separator, we might wish to move it further so user do not delete separator by mistake.
        // because deletion of separator will cause stop formatting that should not happen by mistake
        if (!isCursorAtEnd) {
            while (0 < finalCursorPosition - 1 && !PhoneNumberUtils.isNonSeparator(formatted.charAt(finalCursorPosition - 1))) {
                finalCursorPosition--;
            }
        }

        //Now we have everything calculated, set this values in
        try {
            if (formatted != null) {
                mSelfChange = true;
                s.replace(0, s.length(), formatted, 0, formatted.length());
                mSelfChange = false;
                lastFormatted = s;
                Selection.setSelection(s, finalCursorPosition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * this will format the number in international format (only).
     */
    private String reformat(CharSequence s) {
        String inputNumber = s.toString().replace("\\s-", "");

        Phonenumber.PhoneNumber nationalNumber = phoneNumberUtil.getExampleNumberForType(countryNameCode, PhoneNumberUtil.PhoneNumberType.MOBILE);
        int countryCode = nationalNumber.getCountryCode();
        CCPCountryGroup countryGroup = currentCountryGroup.getCountryGroupForPhoneCode(countryCode);
        String countrySubCode =
                (countryGroup != null && countryGroup.getNameCodeToAreaCodesMap(countryNameCode) != null)
                        ? countryGroup.getNameCodeToAreaCodesMap(countryNameCode).split("/")[0]
                        : "";

        String fullNumber = "";
        String internationalFormatted = "";

        String countryCallingCode = String.format(Locale.getDefault(), "+%d%s", countryCode, countrySubCode);

        if (internationalOnly) {
            fullNumber = countryCallingCode + inputNumber;
        }

        mFormatter.clear();
        for (int i = 0; i < fullNumber.length(); i++) {
            char c = fullNumber.charAt(i);

            if (PhoneNumberUtils.isNonSeparator(c)) {
                char lastNonSeparator = c;

                if (lastNonSeparator != 0) {
                    internationalFormatted = mFormatter.inputDigit(lastNonSeparator);
                }
            }
        }

        internationalFormatted = internationalFormatted.trim();

        String reg = String.format(
                Locale.getDefault(),
                "(\\+\\d{%d}[\\s-]?\\d{%s}[\\s-]?)",
                String.valueOf(countryCode).length(),
                countrySubCode.length());

        return internationalFormatted.replaceAll(reg, "");
    }

    private void stopFormatting() {
        mStopFormatting = true;
        mFormatter.clear();
    }

    private boolean hasSeparator(final CharSequence s, final int start, final int count) {
        for (int i = start; i < start + count; i++) {
            char c = s.charAt(i);
            if (!PhoneNumberUtils.isNonSeparator(c)) {
                return true;
            }
        }
        return false;
    }
}
