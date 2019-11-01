package com.gbksoft.countrycodepickerlib;

import android.content.res.TypedArray;

public class PropertiesPicker {

    /**
     * When width is "match_parent", this gravity will decide the placement of text.
     */
    public enum TextGravity {
        LEFT(-1), CENTER(0), RIGHT(1);

        int enumIndex;

        TextGravity(int i) {
            enumIndex = i;
        }
    }

    private TextGravity currentTextGravity;

    private boolean showFlag = true;
    private boolean showNameCode = true;
    private boolean showFullName = false;
    private boolean showPhoneCode = true;
    private boolean showArrow = true;

    private boolean autoDetectLanguageEnabled = false;
    private boolean autoDetectCountryEnabled = false;
    private boolean numberAutoFormattingEnabled = true;
    private boolean hintExampleNumberEnabled = false;
    private PhoneNumberType hintExampleNumberType = PhoneNumberType.MOBILE;

    public PropertiesPicker() { }

    void init(TypedArray a) {

        //hide nameCode. If someone wants only phone code to avoid name collision for same country phone code.
        showNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showNameCode, true);
        //show full name of the country
        showFullName = a.getBoolean(R.styleable.CountryCodePicker_ccp_showFullName, false);
        //show phone code.
        showPhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_showPhoneCode, true);
        //show arrow
        showArrow = a.getBoolean(R.styleable.CountryCodePicker_ccp_showArrow, true);

        //auto detect language
        autoDetectLanguageEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectLanguage, false);
        //auto detect county
        autoDetectCountryEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_autoDetectCountry, false);
        //number auto formatting
        numberAutoFormattingEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_autoFormatNumber, true);
        //example number hint enabled?
        hintExampleNumberEnabled = a.getBoolean(R.styleable.CountryCodePicker_ccp_hintExampleNumber, false);
        //example number hint type
        int hintNumberTypeIndex = a.getInt(R.styleable.CountryCodePicker_ccp_hintExampleNumberType, 0);
        hintExampleNumberType = PhoneNumberType.values()[hintNumberTypeIndex];
    }

    //==============================================================================================

    TextGravity getCurrentTextGravity() {
        return currentTextGravity;
    }

    void setCurrentTextGravity(TextGravity textGravity) {
        currentTextGravity = textGravity;
    }

    public boolean isShowFlag() {
        return showFlag;
    }

    public void showFlag(boolean showFlag) {
        this.showFlag = showFlag;
    }

    /**
     * If nameCode of country in CCP view is not required use this to show/hide country name code of ccp view.
     *
     * @param showNameCode true will show country name code in ccp view, it will result " (IN) +91 "
     *                     false will remove country name code from ccp view, it will result  " +91 "
     */
    public void showNameCode(boolean showNameCode) {
        this.showNameCode = showNameCode;
    }

    public boolean isShowNameCode() {
        return showNameCode;
    }

    public void showFullName(boolean showFullName) {
        this.showFullName = showFullName;
    }

    public boolean isShowFullName() {
        return showFullName;
    }

    public boolean isShowPhoneCode() {
        return showPhoneCode;
    }

    /**
     * To show/hide phone code from ccp view
     *
     * @param ccpShowPhoneCode
     */
    public void showPhoneCode(boolean ccpShowPhoneCode) {
        this.showPhoneCode = ccpShowPhoneCode;
    }

    public boolean isShowArrow() {
        return showArrow;
    }

    /**
     * This can change visility of arrow.
     *
     * @param showArrow true will show arrow and false will hide arrow from there.
     */
    public void showArrow(boolean showArrow) {
        this.showArrow = showArrow;
    }

    //==============================================================================================


    public void setAutoDetectLanguageEnabled(boolean autoDetectLanguageEnabled) {
        this.autoDetectLanguageEnabled = autoDetectLanguageEnabled;
    }

    public boolean isAutoDetectLanguageEnabled() {
        return autoDetectLanguageEnabled;
    }

    public void setAutoDetectCountryEnabled(boolean autoDetectCountryEnabled) {
        this.autoDetectCountryEnabled = autoDetectCountryEnabled;
    }

    public boolean isAutoDetectCountryEnabled() {
        return autoDetectCountryEnabled;
    }

    /**
     * This will set boolean for numberAutoFormattingEnabled
     *
     * @param numberAutoFormattingEnabled
     */
    public void setNumberAutoFormattingEnabled(boolean numberAutoFormattingEnabled) {
        this.numberAutoFormattingEnabled = numberAutoFormattingEnabled;
    }

    public boolean isNumberAutoFormattingEnabled() {
        return numberAutoFormattingEnabled;
    }

    public void setHintExampleNumberEnabled(boolean hintExampleNumberEnabled) {
        this.hintExampleNumberEnabled = hintExampleNumberEnabled;
    }

    public boolean isHintExampleNumberEnabled() {
        return hintExampleNumberEnabled;
    }

    public void setHintExampleNumberType(PhoneNumberType hintExampleNumberType) {
        this.hintExampleNumberType = hintExampleNumberType;
    }

    public PhoneNumberType getHintExampleNumberType() {
        return hintExampleNumberType;
    }
}