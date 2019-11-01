package com.gbksoft.countrycodepickerlib;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.text.Collator;
import java.util.List;
import java.util.Locale;

class CCPCountry implements Comparable<CCPCountry> {

    private static String TAG = "Class Country";

    private String nameCode;
    private String phoneCode;
    private String name;
    private String englishName;
    private int flagResId = UtilsFlag.DEFAULT_FLAG_RES;

    CCPCountry() {

    }

    CCPCountry(Context context, int nameCodeResId, int phoneCodeResId, int nameResId, int flagResId) {
        this.nameCode = context.getString(nameCodeResId).toUpperCase(Locale.ROOT);
        this.phoneCode = context.getString(phoneCodeResId);
        this.name = context.getString(nameResId);
        this.englishName = context.getString(nameResId);
        this.flagResId = flagResId;
    }

    CCPCountry(Context context, int nameCodeResId, int phoneCodeResId, int nameResId) {
        this.nameCode = context.getString(nameCodeResId).toUpperCase(Locale.ROOT);
        this.phoneCode = context.getString(phoneCodeResId);
        this.name = context.getString(nameResId);
        this.englishName = context.getString(nameResId);
    }

    CCPCountry(String nameCode, String phoneCode, String name, int flagResId) {
        this.nameCode = nameCode.toUpperCase(Locale.ROOT);
        this.phoneCode = phoneCode;
        this.name = name;
        this.flagResId = flagResId;
    }

    /**
     * Search a country which matches @param code.
     *
     * @param context
     * @param preferredCountries is list of preference countries.
     * @param code               phone code. i.e "91" or "1"
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     * if same code (e.g. +1) available for more than one country ( US, canada) , this function will return preferred country.
     */
    static CCPCountry getCountryForCode(Context context, List<CCPCountry> preferredCountries, String code) {
        /**
         * check in preferred countries
         */
        if (preferredCountries != null && !preferredCountries.isEmpty()) {
            for (CCPCountry CCPCountry : preferredCountries) {
                if (CCPCountry.getPhoneCode().equals(code)) {
                    return CCPCountry;
                }
            }
        }

        for (CCPCountry CCPCountry : UtilsCountry.getCountriesList(context)) {
            if (CCPCountry.getPhoneCode().equals(code)) {
                return CCPCountry;
            }
        }
        return null;
    }

    /**
     * @param code phone code. i.e "91" or "1"
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     * if same code (e.g. +1) available for more than one country ( US, canada) , this function will return preferred country.
     * @avoid Search a country which matches @param code. This method is just to support correct preview
     */
    static CCPCountry getCountryForCodeFromEnglishList(Context context, String code) {
        List<CCPCountry> countries;
        countries = UtilsCountry.getCountriesList(context);

        for (CCPCountry ccpCountry : countries) {
            if (ccpCountry.getPhoneCode().equals(code)) {
                return ccpCountry;
            }
        }
        return null;
    }

    static List<CCPCountry> getCustomMasterCountryList(Context context, CountryCodePicker codePicker) {
        codePicker.refreshCustomMasterList();
        if (codePicker.customMasterCountriesList != null && codePicker.customMasterCountriesList.size() > 0) {
            return codePicker.getCustomMasterCountriesList();
        } else {
            return UtilsCountry.getCountriesList(context);
        }
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param context
     * @param customMasterCountriesList
     * @param nameCode                  country name code. i.e US or us or Au. See countries.xml for all code names.  @return Country that has phone code as @param code.
     */
    static CCPCountry getCountryForNameCodeFromCustomMasterList(Context context, List<CCPCountry> customMasterCountriesList, String nameCode) {
        if (customMasterCountriesList == null || customMasterCountriesList.size() == 0) {
            return getCountryForNameCodeFromLibraryMasterList(context, nameCode);
        } else {
            for (CCPCountry ccpCountry : customMasterCountriesList) {
                if (ccpCountry.getNameCode().equalsIgnoreCase(nameCode)) {
                    return ccpCountry;
                }
            }
        }
        return null;
    }

    /**
     * Search a country which matches @param nameCode.
     *
     * @param context
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    static CCPCountry getCountryForNameCodeFromLibraryMasterList(Context context, String nameCode) {
        List<CCPCountry> countries;
        countries = UtilsCountry.getCountriesList(context);
        for (CCPCountry ccpCountry : countries) {
            if (ccpCountry.getNameCode().equalsIgnoreCase(nameCode)) {
                return ccpCountry;
            }
        }
        return null;
    }

    /**
     * Search a country which matches @param nameCode.
     * This searches through local english name list. This should be used only for the preview purpose.
     *
     * @param nameCode country name code. i.e US or us or Au. See countries.xml for all code names.
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    static CCPCountry getCountryForNameCodeFromEnglishList(Context context, String nameCode) {
        List<CCPCountry> countries;
        countries = UtilsCountry.getCountriesList(context);
        for (CCPCountry CCPCountry : countries) {
            if (CCPCountry.getNameCode().equalsIgnoreCase(nameCode)) {
                return CCPCountry;
            }
        }
        return null;
    }

    /**
     * Search a country which matches @param code.
     *
     * @param context
     * @param preferredCountries list of country with priority,
     * @param code               phone code. i.e 91 or 1
     * @return Country that has phone code as @param code.
     * or returns null if no country matches given code.
     */
    static CCPCountry getCountryForCode(Context context, List<CCPCountry> preferredCountries, int code) {
        return getCountryForCode(context, preferredCountries, code + "");
    }

    /**
     * Finds country code by matching substring from left to right from full number.
     * For example. if full number is +819017901357
     * function will ignore "+" and try to find match for first character "8"
     * if any country found for code "8", will return that country. If not, then it will
     * try to find country for "81". and so on till first 3 characters ( maximum number of characters in country code is 3).
     *
     * @param context
     * @param preferredCountries countries of preference
     * @param fullNumber         full number ( "+" (optional)+ country code + carrier number) i.e. +819017901357 / 819017901357 / 918866667722
     * @return Country JP +81(Japan) for +819017901357 or 819017901357
     * Country IN +91(India) for  918866667722
     * null for 2956635321 ( as neither of "2", "29" and "295" matches any country code)
     */
    static CCPCountry getCountryForNumber(Context context, List<CCPCountry> preferredCountries, String fullNumber) {
        int firstDigit;
        //String plainNumber = PhoneNumberUtil.getInstance().normalizeDigitsOnly(fullNumber);
        if (fullNumber.length() != 0) {
            if (fullNumber.charAt(0) == '+') {
                firstDigit = 1;
            } else {
                firstDigit = 0;
            }
            CCPCountry ccpCountry = null;
            for (int i = firstDigit; i <= fullNumber.length(); i++) {
                String code = fullNumber.substring(firstDigit, i);
                CCPCountryGroup countryGroup = null;
                try {
                    countryGroup = CCPCountryGroup.getCountryGroupForPhoneCode(Integer.parseInt(code));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (countryGroup != null) {
                    int areaCodeStartsAt = firstDigit + code.length();
                    //when phone number covers area code too.
                    if (fullNumber.length() >= areaCodeStartsAt + countryGroup.getAreaCodeLength()) {
                        String areaCode = fullNumber.substring(areaCodeStartsAt, areaCodeStartsAt + countryGroup.getAreaCodeLength());
                        return countryGroup.getCountryForAreaCode(context, areaCode);
                    } else {
                        return getCountryForNameCodeFromLibraryMasterList(context, countryGroup.getDefaultNameCode());
                    }
                } else {
                    ccpCountry = CCPCountry.getCountryForCode(context, preferredCountries, code);
                    if (ccpCountry != null) {
                        return ccpCountry;
                    }
                }
            }
        }
        //it reaches here means, phone number has some problem.
        return null;
    }

    /**
     * Finds country code by matching substring from left to right from full number.
     * For example. if full number is +819017901357
     * function will ignore "+" and try to find match for first character "8"
     * if any country found for code "8", will return that country. If not, then it will
     * try to find country for "81". and so on till first 3 characters ( maximum number of characters in country code is 3).
     *
     * @param context
     * @param fullNumber full number ( "+" (optional)+ country code + carrier number) i.e. +819017901357 / 819017901357 / 918866667722
     * @return Country JP +81(Japan) for +819017901357 or 819017901357
     * Country IN +91(India) for  918866667722
     * null for 2956635321 ( as neither of "2", "29" and "295" matches any country code)
     */
    static CCPCountry getCountryForNumber(Context context, String fullNumber) {
        return getCountryForNumber(context, null, fullNumber);
    }

    String getEnglishName() {
        return englishName;
    }

    void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    int getFlagID() {
        if (flagResId == UtilsFlag.DEFAULT_FLAG_RES) {
            flagResId = UtilsFlag.getResID(this);
        }
        return flagResId;
    }

    String getNameCode() {
        return nameCode;
    }

    void setNameCode(String nameCode) {
        this.nameCode = nameCode;
    }

    String getPhoneCode() {
        return phoneCode;
    }

    void setPhoneCode(String phoneCode) {
        this.phoneCode = phoneCode;
    }

    String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    int getFlagResId() {
        return flagResId;
    }

    void setFlagResId(int flagResId) {
        this.flagResId = flagResId;
    }

    void log() {
        try {
            Log.d(TAG, "Country->" + nameCode + ":" + phoneCode + ":" + name);
        } catch (NullPointerException ex) {
            Log.d(TAG, "Null");
        }
    }

    String logString() {
        return nameCode.toUpperCase() + " +" + phoneCode + "(" + name + ")";
    }

    /**
     * If country have query word in name or name code or phone code, this will return true.
     *
     * @param query
     * @return
     */
    boolean isEligibleForQuery(String query) {
        query = query.toLowerCase();
        return getName().toLowerCase().contains(query)
                || getNameCode().toLowerCase().contains(query)
                || getPhoneCode().toLowerCase().contains(query)
                || getEnglishName().toLowerCase().contains(query);
    }

    @Override
    public int compareTo(@NonNull CCPCountry o) {
        return Collator.getInstance().compare(getName(), o.getName());
    }
}
