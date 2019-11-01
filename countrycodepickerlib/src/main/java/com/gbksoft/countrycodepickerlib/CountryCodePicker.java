package com.gbksoft.countrycodepickerlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CountryCodePicker extends LinearLayout {

    private static final String TAG = "CCP";

    static final int DEFAULT_UNSET = -99;

    private static final int LIB_DEFAULT_COUNTRY_CODE = 91;
    private static final int TEXT_GRAVITY_CENTER = 0;
    private static final String ANDROID_NAME_SPACE = "http://schemas.android.com/apk/res/android";
    private static final String CCP_PREF_FILE = "CCP_PREF_FILE";

    private Context context;
    private View holderView;
    private LayoutInflater mInflater;
    private TextView selectedCountry;
    private EditText registeredCarrierNumber;
    private ImageView arrow;
    private ImageView flag;
    private FrameLayout flagLayoutBorder;
    private FrameLayout flagLayout;

    private int defaultCountryCode;
    private String defaultCountryNameCode;


    private CCPCountry selectedCCPCountry;
    private CCPCountry defaultCCPCountry;
    private CountryCodePicker codePicker;

    // see attr.xml to see corresponding values for pref
    private AutoDetectionPref selectedAutoDetectionPref = AutoDetectionPref.SIM_NETWORK_LOCALE;
    private PhoneNumberUtil phoneUtil;

    private PropertiesPicker propertiesPicker = new PropertiesPicker();
    private PropertiesDropDown propertiesDropDown = new PropertiesDropDown();

    private String countryPreference;
    List<CCPCountry> preferredCountries;
    List<CCPCountry> customMasterCountriesList;

    private boolean detectCountryWithAreaCode = true;
    private boolean internationalFormattingOnly = true;

    private String selectionMemoryTag = "ccp_last_selection";
    private int contentColor = DEFAULT_UNSET;
    private int arrowColor = DEFAULT_UNSET;
    private int borderFlagColor;
    private int ccpTextgGravity = TEXT_GRAVITY_CENTER;

    //this will be "AU,IN,US"
    private String customMasterCountriesParam;
    private String excludedCountriesParam;
    //this will be put("ua", R.drawable.flag_ua)
    private Map<String, Integer> customFlagsMap;

    private boolean ccpClickable = true;
    private String xmlWidth = "notSet";
    private TextWatcher validityTextWatcher;
    private InternationalPhoneTextWatcher formattingTextWatcher;
    private boolean reportedValidity;
    private TextWatcher areaCodeCountryDetectorTextWatcher;
    private boolean countryDetectionBasedOnAreaAllowed;
    private String lastCheckedAreaCode = null;
    private int lastCursorPosition = 0;
    private boolean countryChangedDueToAreaCode = false;

    private OnCountryChangeListener onCountryChangeListener;
    private PhoneNumberValidityChangeListener phoneNumberValidityChangeListener;
    private FailureListener failureListener;

    private CCPCountryGroup currentCountryGroup;
    private View.OnClickListener customClickListener;

    private AsYouTypeFormatter mFormatter;
    private PhoneNumberUtil phoneNumberUtil;

    public CCPCountryGroup getCurrentCountryGroup() {
        return currentCountryGroup;
    }

    private View.OnClickListener countryCodeHolderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (customClickListener == null) {
                if (isCcpClickable()) {
                    if (propertiesDropDown.isInitialScrollToSelectionEnabled()) {
                        showDropDown(getSelectedCountryNameCode());
                    } else {
                        showDropDown();
                    }
                }
            } else {
                customClickListener.onClick(v);
            }
        }
    };

    //==============================================================================================

    public CountryCodePicker(Context context) {
        super(context);
        this.context = context;
        init(null);
    }

    public CountryCodePicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(attrs);
    }

    public CountryCodePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    /**
     * This will set boolean for numberAutoFormattingEnabled and refresh formattingTextWatcher
     *
     * @param numberAutoFormattingEnabled
     */
    public void setNumberAutoFormattingEnabled(boolean numberAutoFormattingEnabled) {
        propertiesPicker.setNumberAutoFormattingEnabled(numberAutoFormattingEnabled);
        if (registeredCarrierNumber != null) {
            updateFormattingTextWatcher();
        }
    }

    private boolean isInternationalFormattingOnlyEnabled() {
        return internationalFormattingOnly;
    }

    /**
     * This will set boolean for internationalFormattingOnly and refresh formattingTextWatcher
     *
     * @param internationalFormattingOnly
     */
    public void setInternationalFormattingOnly(boolean internationalFormattingOnly) {
        this.internationalFormattingOnly = internationalFormattingOnly;
        if (registeredCarrierNumber != null) {
            updateFormattingTextWatcher();
        }
    }

    private void init(AttributeSet attrs) {
        mInflater = LayoutInflater.from(context);

        if (attrs != null) {
            xmlWidth = attrs.getAttributeValue(ANDROID_NAME_SPACE, "layout_width");
        }
        removeAllViewsInLayout();
        //at run time, match parent value returns LayoutParams.MATCH_PARENT ("-1"), for some android xml preview it returns "fill_parent"
        if (attrs != null && xmlWidth != null && (xmlWidth.equals(LayoutParams.MATCH_PARENT + "") || xmlWidth.equals(LayoutParams.FILL_PARENT + "") || xmlWidth.equals("fill_parent") || xmlWidth.equals("match_parent"))) {
            holderView = mInflater.inflate(R.layout.layout_code_picker_full_width, this, true);
        } else {
            holderView = mInflater.inflate(R.layout.layout_code_picker, this, true);
        }

        selectedCountry = holderView.findViewById(R.id.selectedCountry);
        arrow = holderView.findViewById(R.id.arrow);
        flag = holderView.findViewById(R.id.flag);
        flagLayout = holderView.findViewById(R.id.flagLayout);
        flagLayoutBorder = holderView.findViewById(R.id.flagLayoutBorder);
        codePicker = this;
        if (attrs != null) {
            applyCustomProperty(attrs);
        }
        holderView.setOnClickListener(countryCodeHolderClickListener);
    }

    private void applyCustomProperty(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CountryCodePicker, 0, 0);
        //default country code
        try {
            propertiesPicker.init(a);
            propertiesDropDown.init(context, a);

            //detect country from area code
            detectCountryWithAreaCode = a.getBoolean(R.styleable.CountryCodePicker_ccp_areaCodeDetectedCountry, true);

            //international formatting only
            internationalFormattingOnly = a.getBoolean(R.styleable.CountryCodePicker_ccp_internationalFormattingOnly, true);

            //memory tag name for selection
            selectionMemoryTag = a.getString(R.styleable.CountryCodePicker_ccp_selectionMemoryTag);
            if (selectionMemoryTag == null) {
                selectionMemoryTag = "CCP_last_selection";
            }

            //country auto detection pref
            int autoDetectionPrefValue = a.getInt(R.styleable.CountryCodePicker_ccp_countryAutoDetectionPref, 123);
            selectedAutoDetectionPref = AutoDetectionPref.getPrefForValue(String.valueOf(autoDetectionPrefValue));

            refreshArrowViewVisibility();

            //show flag
            showFlag(a.getBoolean(R.styleable.CountryCodePicker_ccp_showFlag, true));

            //custom master list
            customMasterCountriesParam = a.getString(R.styleable.CountryCodePicker_ccp_customMasterCountries);
            excludedCountriesParam = a.getString(R.styleable.CountryCodePicker_ccp_excludedCountries);
            if (!isInEditMode()) {
                refreshCustomMasterList();
            }

            //preference
            countryPreference = a.getString(R.styleable.CountryCodePicker_ccp_countryPreference);
            //as3 is raising problem while rendering preview. to avoid such issue, it will update preferred list only on run time.
            if (!isInEditMode()) {
                refreshPreferredCountries();
            }

            //text gravity
            if (a.hasValue(R.styleable.CountryCodePicker_ccp_textGravity)) {
                ccpTextgGravity = a.getInt(R.styleable.CountryCodePicker_ccp_textGravity, TEXT_GRAVITY_CENTER);
            }
            applyTextGravity(ccpTextgGravity);

            //default country
            //AS 3 has some problem with reading list so this is to make CCP preview work
            defaultCountryNameCode = a.getString(R.styleable.CountryCodePicker_ccp_defaultNameCode);
            boolean setUsingNameCode = false;
            if (defaultCountryNameCode != null && defaultCountryNameCode.length() != 0) {
                if (!isInEditMode()) {
                    if (CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), defaultCountryNameCode) != null) {
                        setUsingNameCode = true;
                        setDefaultCountry(CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), defaultCountryNameCode));
                        setSelectedCountry(defaultCCPCountry);
                    }
                } else {
                    if (CCPCountry.getCountryForNameCodeFromEnglishList(context, defaultCountryNameCode) != null) {
                        setUsingNameCode = true;
                        setDefaultCountry(CCPCountry.getCountryForNameCodeFromEnglishList(context, defaultCountryNameCode));
                        setSelectedCountry(defaultCCPCountry);
                    }
                }

                //when it was not set means something was wrong with name code
                if (!setUsingNameCode) {
                    setDefaultCountry(CCPCountry.getCountryForNameCodeFromEnglishList(context, "IN"));
                    setSelectedCountry(defaultCCPCountry);
                    setUsingNameCode = true;
                }
            }

            //if default country is not set using name code.
            int defaultCountryCode = a.getInteger(R.styleable.CountryCodePicker_ccp_defaultPhoneCode, -1);
            if (!setUsingNameCode && defaultCountryCode != -1) {
                if (!isInEditMode()) {
                    //if invalid country is set using xml, it will be replaced with LIB_DEFAULT_COUNTRY_CODE
                    if (defaultCountryCode != -1 && CCPCountry.getCountryForCode(getContext(), preferredCountries, defaultCountryCode) == null) {
                        defaultCountryCode = LIB_DEFAULT_COUNTRY_CODE;
                    }
                    setDefaultCountryUsingPhoneCode(defaultCountryCode);
                    setSelectedCountry(defaultCCPCountry);
                } else {
                    //when it is in edit mode, we will check in english list only.
                    CCPCountry defaultCountry = CCPCountry.getCountryForCodeFromEnglishList(context, defaultCountryCode + "");
                    if (defaultCountry == null) {
                        defaultCountry = CCPCountry.getCountryForCodeFromEnglishList(context, LIB_DEFAULT_COUNTRY_CODE + "");
                    }
                    setDefaultCountry(defaultCountry);
                    setSelectedCountry(defaultCountry);
                }
            }

            //if default country is not set using nameCode or phone code, let's set library default as default
            if (getDefaultCountry() == null) {
                setDefaultCountry(CCPCountry.getCountryForNameCodeFromEnglishList(context, "IN"));
                if (getSelectedCountry() == null) {
                    setSelectedCountry(defaultCCPCountry);
                }
            }


            //set auto detected country
            if (propertiesPicker.isAutoDetectCountryEnabled() && !isInEditMode()) {
                setAutoDetectedCountry(true);
            }

            //set last selection
            if (propertiesDropDown.rememberLastSelection() && !isInEditMode()) {
                loadLastSelectedCountryInCCP();
            }

            int arrowColor;
            arrowColor = a.getColor(R.styleable.CountryCodePicker_ccp_arrowColor, DEFAULT_UNSET);
            setArrowColor(arrowColor);

            //content color
            int contentColor;
            if (isInEditMode()) {
                contentColor = a.getColor(R.styleable.CountryCodePicker_ccp_contentColor, DEFAULT_UNSET);
            } else {
                contentColor = a.getColor(R.styleable.CountryCodePicker_ccp_contentColor, context.getResources().getColor(R.color.defaultContentColor));
            }
            if (contentColor != DEFAULT_UNSET) {
                setContentColor(contentColor);
            }

            // flag border color
            int borderFlagColor;
            if (isInEditMode()) {
                borderFlagColor = a.getColor(R.styleable.CountryCodePicker_ccp_flagBorderColor, 0);
            } else {
                borderFlagColor = a.getColor(R.styleable.CountryCodePicker_ccp_flagBorderColor, context.getResources().getColor(R.color.defaultBorderFlagColor));
            }
            if (borderFlagColor != 0) {
                setFlagBorderColor(borderFlagColor);
            }

            //text size
            int textSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_textSize, 0);
            if (textSize > 0) {
                selectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                setFlagSize(textSize);
                setArrowSize(textSize);
            }

            //if arrow size is explicitly defined
            int arrowSize = a.getDimensionPixelSize(R.styleable.CountryCodePicker_ccp_arrowSize, 0);
            if (arrowSize > 0) {
                setArrowSize(arrowSize);
            }

            setCcpClickable(a.getBoolean(R.styleable.CountryCodePicker_ccp_clickable, true));

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            selectedCountry.setMaxLines(25);
            selectedCountry.setTextSize(10);
            selectedCountry.setText(sw.toString());
        } finally {
            a.recycle();
        }
    }

    //==============================================================================================

    public PropertiesPicker getPropertiesPicker() {
        return propertiesPicker;
    }

    public PropertiesDropDown getPropertiesDropDown() {
        return propertiesDropDown;
    }

    private void refreshArrowViewVisibility() {
        if (propertiesPicker.isShowArrow()) {
            arrow.setVisibility(VISIBLE);
        } else {
            arrow.setVisibility(GONE);
        }
    }

    /**
     * this will read last selected country name code from the shared pref.
     * if that name code is not null, load that country in the CCP
     * else leaves as it is.(when used for the first time)
     */
    private void loadLastSelectedCountryInCCP() {
        //get the shared pref
        SharedPreferences sharedPref = context.getSharedPreferences(CCP_PREF_FILE, Context.MODE_PRIVATE);

        // read last selection value
        String lastSelectedCountryNameCode = sharedPref.getString(selectionMemoryTag, null);

        //if last selection value is not null, load it into the CCP
        if (lastSelectedCountryNameCode != null) {
            setCountryForNameCode(lastSelectedCountryNameCode);
        }
    }

    /**
     * This will store the selected name code in the preferences
     *
     * @param selectedCountryNameCode name code of the selected country
     */
    void storeSelectedCountryNameCode(String selectedCountryNameCode) {
        //get the shared pref
        SharedPreferences sharedPref = context.getSharedPreferences(CCP_PREF_FILE, Context.MODE_PRIVATE);

        //we want to write in shared pref, so lets get editor for it
        SharedPreferences.Editor editor = sharedPref.edit();

        // add our last selection country name code in pref
        editor.putString(selectionMemoryTag, selectedCountryNameCode);

        //finally save it...
        editor.apply();
    }

    /**
     * To show/hide phone code from ccp view
     *
     * @param ccpShowPhoneCode
     */
    public void setCcpShowPhoneCode(boolean ccpShowPhoneCode) {
        this.propertiesPicker.showPhoneCode(ccpShowPhoneCode);
        setSelectedCountry(selectedCCPCountry);
    }

    /**
     * @return registered dropDown event listener
     */
    protected DropDownEventsListener getDropDownEventsListener() {
        if(propertiesDropDown != null){
            return propertiesDropDown.getDropDownEventsListener();
        }
        return null;
    }

    /**
     * Events listener will give call backs on various dropDown events
     *
     * @param dropDownEventsListener
     */
    public void setDropDownEventsListener(DropDownEventsListener dropDownEventsListener) {
        if(propertiesDropDown != null) {
            propertiesDropDown.setDropDownEventsListener(dropDownEventsListener);
        }
    }

    public PropertiesPicker.TextGravity getCurrentTextGravity() {
        return propertiesPicker.getCurrentTextGravity();
    }

    /**
     * When width is set "match_parent", this gravity will set placement of text (Between flag and down arrow).
     *
     * @param textGravity expected placement
     */
    public void setCurrentTextGravity(PropertiesPicker.TextGravity textGravity) {
        propertiesPicker.setCurrentTextGravity(textGravity);
        applyTextGravity(textGravity.enumIndex);
    }

    private void applyTextGravity(int enumIndex) {
        if (enumIndex == PropertiesPicker.TextGravity.LEFT.enumIndex) {
            selectedCountry.setGravity(Gravity.LEFT);
        } else if (enumIndex == PropertiesPicker.TextGravity.CENTER.enumIndex) {
            selectedCountry.setGravity(Gravity.CENTER);
        } else {
            selectedCountry.setGravity(Gravity.RIGHT);
        }
    }

    private CCPCountry getDefaultCountry() {
        return defaultCCPCountry;
    }

    private void setDefaultCountry(CCPCountry defaultCCPCountry) {
        this.defaultCCPCountry = defaultCCPCountry;
        //        Log.d(TAG, "Setting default country:" + defaultCountry.logString());
    }

    private CCPCountry getSelectedCountry() {
        if (selectedCCPCountry == null) {
            setSelectedCountry(getDefaultCountry());
        }
        return selectedCCPCountry;
    }

    void setSelectedCountry(CCPCountry selectedCCPCountry) {
        //force disable area code country detection
        countryDetectionBasedOnAreaAllowed = false;
        lastCheckedAreaCode = "";

        //as soon as country is selected, textView should be updated
        if (selectedCCPCountry == null) {
            selectedCCPCountry = CCPCountry.getCountryForCode(getContext(), preferredCountries, defaultCountryCode);
            if (selectedCCPCountry == null) {
                return;
            }
        }

        this.selectedCCPCountry = selectedCCPCountry;

        String displayText = "";

        // add flag if required
        if (propertiesPicker.isShowFlag() && propertiesDropDown.useEmoji()) {
            if (isInEditMode()) {
//                android studio preview shows huge space if 0 width space is not added.
                if (propertiesDropDown.useDummyEmojiForPreview()) {
                    //show chequered flag if dummy preview is expected.
                    displayText += "\uD83C\uDFC1\u200B ";
                } else {
                    displayText += UtilsFlag.getEmoji(selectedCCPCountry) + "\u200B ";
                }

            } else {
                displayText += UtilsFlag.getEmoji(selectedCCPCountry) + "  ";
            }
        }

        // add full name to if required
        if (propertiesPicker.isShowFullName()) {
            displayText = displayText + selectedCCPCountry.getName();
        }

        // adds full name if required
        if (propertiesPicker.isShowFullName()) {
            displayText += " " + selectedCCPCountry.getName();
        }

        // adds name code if required
        if (propertiesPicker.isShowNameCode()) {
            displayText += " (" + selectedCCPCountry.getNameCode().toUpperCase() + ")";
        }


        // hide phone code if required
        if (propertiesPicker.isShowPhoneCode()) {
            if (displayText.length() > 0) {
                displayText += "  ";
            }
            displayText += "+" + selectedCCPCountry.getPhoneCode();
        }

        selectedCountry.setText(displayText);

        //avoid blank state of ccp
        if (!propertiesPicker.isShowFlag() && displayText.length() == 0) {
            displayText += "+" + selectedCCPCountry.getPhoneCode();
            selectedCountry.setText(displayText);
        }

        if (onCountryChangeListener != null) {
            onCountryChangeListener.onCountrySelected();
        }

        flag.setImageResource(selectedCCPCountry.getFlagID());
        //        Log.d(TAG, "Setting selected country:" + selectedCountry.logString());

        updateFormattingTextWatcher();

        updateHint();

        //notify to registered validity listener
        if (registeredCarrierNumber != null && phoneNumberValidityChangeListener != null) {
            reportedValidity = isValidFullNumber();
            phoneNumberValidityChangeListener.onValidityChanged(reportedValidity);
        }

        //once updates are done, this will release lock
        countryDetectionBasedOnAreaAllowed = true;

        //if the country was auto detected based on area code, this will correct the cursor position.
        if (countryChangedDueToAreaCode) {
            try {
                registeredCarrierNumber.setSelection(lastCursorPosition);
                countryChangedDueToAreaCode = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //update country group
        updateCountryGroup();
    }

    /**
     * update country group
     */
    private void updateCountryGroup() {
        currentCountryGroup = CCPCountryGroup.getCountryGroupForPhoneCode(getSelectedCountryCodeAsInt());
    }

    /**
     * updates hint
     */
    private void updateHint() {
        if (registeredCarrierNumber != null && propertiesPicker.isHintExampleNumberEnabled()) {
            phoneNumberUtil = PhoneNumberUtil.getInstance();
            mFormatter = phoneNumberUtil.getAsYouTypeFormatter(getSelectedCountryNameCode());
            mFormatter.clear();

            Phonenumber.PhoneNumber nationalNumber = phoneNumberUtil.getExampleNumberForType(getSelectedCountryNameCode(), PhoneNumberUtil.PhoneNumberType.MOBILE);
            long nationalNumberLong = nationalNumber.getNationalNumber();

            int countryCode = nationalNumber.getCountryCode();
            CCPCountryGroup countryGroup = getCurrentCountryGroup().getCountryGroupForPhoneCode(countryCode);
            String countrySubCode =
                    (countryGroup != null && countryGroup.getNameCodeToAreaCodesMap(getSelectedCountryNameCode()) != null)
                            ? countryGroup.getNameCodeToAreaCodesMap(getSelectedCountryNameCode()).split("/")[0]
                            : "";

            String countryCallingCode = String.format(Locale.getDefault(), "+%d%s", countryCode, countrySubCode);
            String nationalNumberStr = String.valueOf(nationalNumberLong).substring(countrySubCode.split("/")[0].length());
            String fullNumber = countryCallingCode + nationalNumberStr.replace("\\s-", "");

            String internationalFormatted = "";

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

            registeredCarrierNumber.setHint(internationalFormatted.replaceAll(reg, ""));
        }
    }

    /**
     * this function maps CountryCodePicker.PhoneNumberType to PhoneNumberUtil.PhoneNumberType.
     *
     * @return respective PhoneNumberUtil.PhoneNumberType based on selected CountryCodePicker.PhoneNumberType.
     */
    public PhoneNumberUtil.PhoneNumberType getSelectedHintNumberType() {
        switch (propertiesPicker.getHintExampleNumberType()) {
            default:
            case MOBILE:
                return PhoneNumberUtil.PhoneNumberType.MOBILE;
            case FIXED_LINE:
                return PhoneNumberUtil.PhoneNumberType.FIXED_LINE;
            case FIXED_LINE_OR_MOBILE:
                return PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE;
            case TOLL_FREE:
                return PhoneNumberUtil.PhoneNumberType.TOLL_FREE;
            case PREMIUM_RATE:
                return PhoneNumberUtil.PhoneNumberType.PREMIUM_RATE;
            case SHARED_COST:
                return PhoneNumberUtil.PhoneNumberType.SHARED_COST;
            case VOIP:
                return PhoneNumberUtil.PhoneNumberType.VOIP;
            case PERSONAL_NUMBER:
                return PhoneNumberUtil.PhoneNumberType.PERSONAL_NUMBER;
            case PAGER:
                return PhoneNumberUtil.PhoneNumberType.PAGER;
            case UAN:
                return PhoneNumberUtil.PhoneNumberType.UAN;
            case VOICEMAIL:
                return PhoneNumberUtil.PhoneNumberType.VOICEMAIL;
            case UNKNOWN:
                return PhoneNumberUtil.PhoneNumberType.UNKNOWN;
        }
    }

    private void updateFormattingTextWatcher() {
        if (registeredCarrierNumber != null && selectedCCPCountry != null) {
            String enteredValue = registeredCarrierNumber.getText().toString();
            String digitsValue = PhoneNumberUtil.normalizeDigitsOnly(enteredValue);

            if (formattingTextWatcher != null) {
                registeredCarrierNumber.removeTextChangedListener(formattingTextWatcher);
            }

            if (areaCodeCountryDetectorTextWatcher != null) {
                registeredCarrierNumber.removeTextChangedListener(areaCodeCountryDetectorTextWatcher);
            }

            if (propertiesPicker.isNumberAutoFormattingEnabled()) {
                formattingTextWatcher = new InternationalPhoneTextWatcher(
                        context,
                        getCurrentCountryGroup(),
                        getSelectedCountryNameCode(),
                        getSelectedCountryCodeAsInt(), internationalFormattingOnly);
                registeredCarrierNumber.addTextChangedListener(formattingTextWatcher);
            }

            //if country detection from area code is enabled, then it will add areaCodeCountryDetectorTextWatcher
            if (detectCountryWithAreaCode) {
                areaCodeCountryDetectorTextWatcher = getCountryDetectorTextWatcher();
                registeredCarrierNumber.addTextChangedListener(areaCodeCountryDetectorTextWatcher);
            }

            //text watcher stops working when it finds non digit character in previous phone code. This will reset its function
            registeredCarrierNumber.setText("");
            registeredCarrierNumber.setText(digitsValue);
            registeredCarrierNumber.setSelection(registeredCarrierNumber.getText().length());
        } else {
            if (registeredCarrierNumber == null) {
                Log.v(TAG, "updateFormattingTextWatcher: EditText not registered " + selectionMemoryTag);
            } else {
                Log.v(TAG, "updateFormattingTextWatcher: selected country is null " + selectionMemoryTag);
            }
        }
    }

    /**
     * This updates country dynamically as user types in area code
     *
     * @return
     */
    private TextWatcher getCountryDetectorTextWatcher() {
        if (registeredCarrierNumber != null) {
            if (areaCodeCountryDetectorTextWatcher == null) {
                areaCodeCountryDetectorTextWatcher = new TextWatcher() {
                    String lastCheckedNumber = null;

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        CCPCountry selectedCountry = getSelectedCountry();
                        if (selectedCountry != null && (lastCheckedNumber == null || !lastCheckedNumber.equals(s.toString())) && countryDetectionBasedOnAreaAllowed) {
                            //possible countries
                            if (currentCountryGroup != null) {
                                String enteredValue = registeredCarrierNumber.getText().toString();
                                if (enteredValue.length() >= currentCountryGroup.getAreaCodeLength()) {
                                    String digitsValue = PhoneNumberUtil.normalizeDigitsOnly(enteredValue);
                                    if (digitsValue.length() >= currentCountryGroup.getAreaCodeLength()) {
                                        String currentAreaCode = digitsValue.substring(0, currentCountryGroup.getAreaCodeLength());
                                        if (!currentAreaCode.equals(lastCheckedAreaCode)) {
                                            CCPCountry detectedCountry = currentCountryGroup.getCountryForAreaCode(context, currentAreaCode);
                                            if (!detectedCountry.equals(selectedCountry)) {
                                                countryChangedDueToAreaCode = true;
                                                lastCursorPosition = Selection.getSelectionEnd(s);
                                                setSelectedCountry(detectedCountry);
                                            }
                                            lastCheckedAreaCode = currentAreaCode;
                                        }
                                    }
                                }
                            }
                            lastCheckedNumber = s.toString();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {


                    }
                };
            }
        }
        return areaCodeCountryDetectorTextWatcher;
    }

    /**
     * This function will
     * - remove existing, if any, validityTextWatcher
     * - prepare new validityTextWatcher
     * - attach validityTextWatcher
     * - do initial reporting to watcher
     */
    private void updateValidityTextWatcher() {
        try {
            registeredCarrierNumber.removeTextChangedListener(validityTextWatcher);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //initial REPORTING
        reportedValidity = isValidFullNumber();
        if (phoneNumberValidityChangeListener != null) {
            phoneNumberValidityChangeListener.onValidityChanged(reportedValidity);
        }

        validityTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (phoneNumberValidityChangeListener != null) {
                    boolean currentValidity;
                    currentValidity = isValidFullNumber();
                    if (currentValidity != reportedValidity) {
                        reportedValidity = currentValidity;
                        phoneNumberValidityChangeListener.onValidityChanged(reportedValidity);
                    }
                }
            }
        };

        registeredCarrierNumber.addTextChangedListener(validityTextWatcher);
    }

    /**
     * this will load preferredCountries based on countryPreference
     */
    void refreshPreferredCountries() {
        if (countryPreference == null || countryPreference.length() == 0) {
            preferredCountries = null;
        } else {
            List<CCPCountry> localCCPCountryList = new ArrayList<>();
            for (String nameCode : countryPreference.split(",")) {
                CCPCountry ccpCountry = CCPCountry.getCountryForNameCodeFromCustomMasterList(getContext(), customMasterCountriesList, nameCode);
                if (ccpCountry != null) {
                    if (!isAlreadyInList(ccpCountry, localCCPCountryList)) { //to avoid duplicate entry of country
                        localCCPCountryList.add(ccpCountry);
                    }
                }
            }

            if (localCCPCountryList.size() == 0) {
                preferredCountries = null;
            } else {
                preferredCountries = localCCPCountryList;
            }
        }
    }

    /**
     * this will load preferredCountries based on countryPreference
     */
    void refreshCustomMasterList() {
        //if no custom list specified then check for exclude list
        if (customMasterCountriesParam == null || customMasterCountriesParam.length() == 0) {
            //if excluded param or custom flags are also blank, then do nothing
            if ((excludedCountriesParam != null && excludedCountriesParam.length() != 0 )
                    || (customFlagsMap != null && customFlagsMap.size() > 0)){
                if(excludedCountriesParam != null) {
                    excludedCountriesParam = excludedCountriesParam.toLowerCase();
                }
                List<CCPCountry> libraryMasterList = UtilsCountry.getCountriesList(context);
                List<CCPCountry> localCCPCountryList = new ArrayList<>();
                for (CCPCountry ccpCountry : libraryMasterList) {

                    ccpCountry = getNewCCPCountryIfFlagResWasChanged(ccpCountry);
                    //if the country name code is in the excluded list, avoid it.
                    if (excludedCountriesParam == null || !excludedCountriesParam.contains(ccpCountry.getNameCode().toLowerCase())) {
                        localCCPCountryList.add(ccpCountry);
                    }
                }

                if (localCCPCountryList.size() > 0) {
                    customMasterCountriesList = localCCPCountryList;
                } else {
                    customMasterCountriesList = null;
                }

            } else {
                customMasterCountriesList = null;
            }
        } else {
            //else add custom list
            List<CCPCountry> localCCPCountryList = new ArrayList<>();
            for (String nameCode : customMasterCountriesParam.split(",")) {
                CCPCountry ccpCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), nameCode);
                if (ccpCountry != null) {
                    if (!isAlreadyInList(ccpCountry, localCCPCountryList)) { //to avoid duplicate entry of country
                        ccpCountry = getNewCCPCountryIfFlagResWasChanged(ccpCountry);
                        localCCPCountryList.add(ccpCountry);
                    }
                }
            }

            if (localCCPCountryList.size() == 0) {
                customMasterCountriesList = null;
            } else {
                customMasterCountriesList = localCCPCountryList;
            }
        }
    }

    /**
     *
     * @param ccpCountry
     * @return new instance of CCPCountry class if flag resource was changed for this country or
     * same instance otherwise.
     * Also if flag resource was changed for selectedCCPCountry it will be override with new instance.
     */

    private CCPCountry getNewCCPCountryIfFlagResWasChanged(CCPCountry ccpCountry){
        //if custom flags exist create new instance of country
        //changing resource directly will change it for all other pickers
        if(customFlagsMap != null && customFlagsMap.containsKey(ccpCountry.getNameCode())){
            Integer customFlagResource = customFlagsMap.get(ccpCountry.getNameCode());
            if(customFlagResource != null) {
                ccpCountry = new CCPCountry(ccpCountry.getNameCode(),
                        ccpCountry.getPhoneCode(), ccpCountry.getName(),
                        customFlagResource);
            }
            if(selectedCCPCountry != null && selectedCCPCountry.getNameCode().equals(ccpCountry.getNameCode())){
                selectedCCPCountry = ccpCountry;
            }
        }
        return ccpCountry;
    }

    List<CCPCountry> getCustomMasterCountriesList() {
        return customMasterCountriesList;
    }

    /**
     * @param customMasterCountriesList is list of countries that we need as custom master list
     */
    void setCustomMasterCountriesList(List<CCPCountry> customMasterCountriesList) {
        this.customMasterCountriesList = customMasterCountriesList;
    }

    /**
     * @return comma separated custom master countries' name code. i.e "gb,us,nz,in,pk"
     */
    public String getCustomMasterCountriesParam() {
        return customMasterCountriesParam;
    }

    /**
     * To provide definite set of countries when selection dropDown is opened.
     * Only custom master countries, if defined, will be there is selection dropDown to select from.
     * To set any country in preference, it must be included in custom master countries, if defined
     * When not defined or null or blank is set, it will use library's default master list
     * Custom master list will only limit the visibility of irrelevant country from selection dropDown. But all other functions like setCountryForCodeName() or setFullNumber() will consider all the countries.
     *
     * @param customMasterCountriesParam is country name codes separated by comma. e.g. "us,in,nz"
     *                                   if null or "" , will remove custom countries and library default will be used.
     */
    public void setCustomMasterCountries(String customMasterCountriesParam) {
        this.customMasterCountriesParam = customMasterCountriesParam;
    }

    /**
     * This can be used to remove certain countries from the list by keeping all the others.
     * This will be ignored if you have specified your own country master list.
     *
     * @param excludedCountries is country name codes separated by comma. e.g. "us,in,nz"
     *                          null or "" means no country is excluded.
     */
    public void setExcludedCountries(String excludedCountries) {
        this.excludedCountriesParam = excludedCountries;
        refreshCustomMasterList();
    }

    /**
     * @return true if ccp is enabled for click
     */
    boolean isCcpClickable() {
        return ccpClickable;
    }

    /**
     * Allow click and open dropDown
     *
     * @param ccpClickable
     */
    public void setCcpClickable(boolean ccpClickable) {
        this.ccpClickable = ccpClickable;
        if (!ccpClickable) {
            holderView.setOnClickListener(null);
            holderView.setClickable(false);
            holderView.setEnabled(false);
        } else {
            holderView.setOnClickListener(countryCodeHolderClickListener);
            holderView.setClickable(true);
            holderView.setEnabled(true);
        }
    }

    /**
     * This will match name code of all countries of list against the country's name code.
     *
     * @param CCPCountry
     * @param CCPCountryList list of countries against which country will be checked.
     * @return if country name code is found in list, returns true else return false
     */
    private boolean isAlreadyInList(CCPCountry CCPCountry, List<CCPCountry> CCPCountryList) {
        if (CCPCountry != null && CCPCountryList != null) {
            for (CCPCountry iterationCCPCountry : CCPCountryList) {
                if (iterationCCPCountry.getNameCode().equalsIgnoreCase(CCPCountry.getNameCode())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * This function removes possible country code from fullNumber and set rest of the number as carrier number.
     *
     * @param fullNumber combination of country code and carrier number.
     * @param CCPCountry selected country in CCP to detect country code part.
     */
    private String detectCarrierNumber(String fullNumber, CCPCountry CCPCountry) {
        String carrierNumber;
        if (CCPCountry == null || fullNumber == null || fullNumber.isEmpty()) {
            carrierNumber = fullNumber;
        } else {
            int indexOfCode = fullNumber.indexOf(CCPCountry.getPhoneCode());
            if (indexOfCode == -1) {
                carrierNumber = fullNumber;
            } else {
                carrierNumber = fullNumber.substring(indexOfCode + CCPCountry.getPhoneCode().length());
            }
        }
        return carrierNumber;
    }


    /**
     * This method is not encouraged because this might set some other country which have same country code as of yours. e.g 1 is common for US and canada.
     * If you are trying to set US ( and countryPreference is not set) and you pass 1 as @param defaultCountryCode, it will set canada (prior in list due to alphabetical order)
     * Rather use @function setDefaultCountryUsingNameCode("us"); or setDefaultCountryUsingNameCode("US");
     * <p>
     * Default country code defines your default country.
     * Whenever invalid / improper number is found in setCountryForPhoneCode() /  setFullNumber(), it CCP will set to default country.
     * This function will not set default country as selected in CCP. To set default country in CCP call resetToDefaultCountry() right after this call.
     * If invalid defaultCountryCode is applied, it won't be changed.
     *
     * @param defaultCountryCode code of your default country
     *                           if you want to set IN +91(India) as default country, defaultCountryCode =  91
     *                           if you want to set JP +81(Japan) as default country, defaultCountryCode =  81
     */
    @Deprecated
    public void setDefaultCountryUsingPhoneCode(int defaultCountryCode) {
        CCPCountry defaultCCPCountry = CCPCountry.getCountryForCode(getContext(), preferredCountries, defaultCountryCode); //xml stores data in string format, but want to allow only numeric value to country code to user.
        if (defaultCCPCountry == null) { //if no correct country is found
            //            Log.d(TAG, "No country for code " + defaultCountryCode + " is found");
        } else { //if correct country is found, set the country
            this.defaultCountryCode = defaultCountryCode;
            setDefaultCountry(defaultCCPCountry);
        }
    }

    /**
     * Default country name code defines your default country.
     * Whenever invalid / improper name code is found in setCountryForNameCode(), CCP will set to default country.
     * This function will not set default country as selected in CCP. To set default country in CCP call resetToDefaultCountry() right after this call.
     * If invalid defaultCountryCode is applied, it won't be changed.
     *
     * @param defaultCountryNameCode code of your default country
     *                               if you want to set IN +91(India) as default country, defaultCountryCode =  "IN" or "in"
     *                               if you want to set JP +81(Japan) as default country, defaultCountryCode =  "JP" or "jp"
     */
    public void setDefaultCountryUsingNameCode(String defaultCountryNameCode) {
        CCPCountry defaultCCPCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), defaultCountryNameCode); //xml stores data in string format, but want to allow only numeric value to country code to user.
        if (defaultCCPCountry == null) { //if no correct country is found
            //            Log.d(TAG, "No country for nameCode " + defaultCountryNameCode + " is found");
        } else { //if correct country is found, set the country
            this.defaultCountryNameCode = defaultCCPCountry.getNameCode();
            setDefaultCountry(defaultCCPCountry);
        }
    }

    /**
     * @return: Country Code of default country
     * i.e if default country is IN +91(India)  returns: "91"
     * if default country is JP +81(Japan) returns: "81"
     */
    public String getDefaultCountryCode() {
        return defaultCCPCountry.getPhoneCode();
    }

    /**
     * * To get code of default country as Integer.
     *
     * @return integer value of default country code in CCP
     * i.e if default country is IN +91(India)  returns: 91
     * if default country is JP +81(Japan) returns: 81
     */
    public int getDefaultCountryCodeAsInt() {
        int code = 0;
        try {
            code = Integer.parseInt(getDefaultCountryCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * To get code of default country with prefix "+".
     *
     * @return String value of default country code in CCP with prefix "+"
     * i.e if default country is IN +91(India)  returns: "+91"
     * if default country is JP +81(Japan) returns: "+81"
     */
    public String getDefaultCountryCodeWithPlus() {
        return "+" + getDefaultCountryCode();
    }

    /**
     * To get name of default country.
     *
     * @return String value of country name, default in CCP
     * i.e if default country is IN +91(India)  returns: "India"
     * if default country is JP +81(Japan) returns: "Japan"
     */
    public String getDefaultCountryName() {
        return getDefaultCountry().getName();
    }

    /**
     * To get name code of default country.
     *
     * @return String value of country name, default in CCP
     * i.e if default country is IN +91(India)  returns: "IN"
     * if default country is JP +81(Japan) returns: "JP"
     */
    public String getDefaultCountryNameCode() {
        return getDefaultCountry().getNameCode().toUpperCase();
    }

    /**
     * reset the default country as selected country.
     */
    public void resetToDefaultCountry() {
        defaultCCPCountry = CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), getDefaultCountryNameCode());
        setSelectedCountry(defaultCCPCountry);
    }

    /**
     * To get code of selected country.
     *
     * @return String value of selected country code in CCP
     * i.e if selected country is IN +91(India)  returns: "91"
     * if selected country is JP +81(Japan) returns: "81"
     */
    public String getSelectedCountryCode() {
        return getSelectedCountry().getPhoneCode();
    }

    /**
     * To get code of selected country with prefix "+".
     *
     * @return String value of selected country code in CCP with prefix "+"
     * i.e if selected country is IN +91(India)  returns: "+91"
     * if selected country is JP +81(Japan) returns: "+81"
     */
    public String getSelectedCountryCodeWithPlus() {
        return "+" + getSelectedCountryCode();
    }

    /**
     * * To get code of selected country as Integer.
     *
     * @return integer value of selected country code in CCP
     * i.e if selected country is IN +91(India)  returns: 91
     * if selected country is JP +81(Japan) returns: 81
     */
    public int getSelectedCountryCodeAsInt() {
        int code = 0;
        try {
            code = Integer.parseInt(getSelectedCountryCode());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return code;
    }

    /**
     * To get name of selected country.
     *
     * @return String value of country name, selected in CCP
     * i.e if selected country is IN +91(India)  returns: "India"
     * if selected country is JP +81(Japan) returns: "Japan"
     */
    public String getSelectedCountryName() {
        return getSelectedCountry().getName();
    }

    /**
     * To get name of selected country in English.
     *
     * @return String value of country name in English language, selected in CCP
     * i.e if selected country is IN +91(India)  returns: "India" no matter what language is currently selected.
     * if selected country is JP +81(Japan) returns: "Japan"
     */
    public String getSelectedCountryEnglishName() {
        return getSelectedCountry().getEnglishName();
    }

    /**
     * To get name code of selected country.
     *
     * @return String value of country name, selected in CCP
     * i.e if selected country is IN +91(India)  returns: "IN"
     * if selected country is JP +81(Japan) returns: "JP"
     */
    public String getSelectedCountryNameCode() {
        return getSelectedCountry().getNameCode().toUpperCase();
    }

    /**
     * This will set country with @param countryCode as country code, in CCP
     *
     * @param countryCode a valid country code.
     *                    If you want to set IN +91(India), countryCode= 91
     *                    If you want to set JP +81(Japan), countryCode= 81
     */
    public void setCountryForPhoneCode(int countryCode) {
        CCPCountry ccpCountry = CCPCountry.getCountryForCode(getContext(), preferredCountries, countryCode); //xml stores data in string format, but want to allow only numeric value to country code to user.
        if (ccpCountry == null) {
            if (defaultCCPCountry == null) {
                defaultCCPCountry = ccpCountry.getCountryForCode(getContext(), preferredCountries, defaultCountryCode);
            }
            setSelectedCountry(defaultCCPCountry);
        } else {
            setSelectedCountry(ccpCountry);
        }
    }

    /**
     * This will set country with @param countryNameCode as country name code, in CCP
     *
     * @param countryNameCode a valid country name code.
     *                        If you want to set IN +91(India), countryCode= IN
     *                        If you want to set JP +81(Japan), countryCode= JP
     */
    public void setCountryForNameCode(String countryNameCode) {
        CCPCountry country = CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), countryNameCode); //xml stores data in string format, but want to allow only numeric value to country code to user.
        if (country == null) {
            if (defaultCCPCountry == null) {
                defaultCCPCountry = country.getCountryForCode(getContext(), preferredCountries, defaultCountryCode);
            }
            setSelectedCountry(defaultCCPCountry);
        } else {
            setSelectedCountry(country);
        }
    }

    /**
     * All functions that work with fullNumber need an editText to write and read carrier number of full number.
     * An editText for carrier number must be registered in order to use functions like setFullNumber() and getFullNumber().
     *
     * @param editTextCarrierNumber - an editText where user types carrier number ( the part of full number other than country code).
     */
    public void registerCarrierNumberEditText(EditText editTextCarrierNumber) {
        this.registeredCarrierNumber = editTextCarrierNumber;
        updateValidityTextWatcher();
        updateFormattingTextWatcher();
        updateHint();
    }

    /**
     * If edittext was already registered, this will remove attached textwatchers and set
     * editText to null
     */
    public void deregisterCarrierNumberEditText() {
        if (registeredCarrierNumber != null) {
            // remove validity listener
            try {
                registeredCarrierNumber.removeTextChangedListener(validityTextWatcher);
            } catch (Exception ignored) {

            }

            // if possible, remove formatting textwatcher
            try {
                registeredCarrierNumber.removeTextChangedListener(formattingTextWatcher);
            } catch (Exception ignored) {

            }

            registeredCarrierNumber.setHint("");

            registeredCarrierNumber = null;
        }
    }

    private Phonenumber.PhoneNumber getEnteredPhoneNumber() throws NumberParseException {
        String carrierNumber = "";
        if (registeredCarrierNumber != null) {
            carrierNumber = PhoneNumberUtil.normalizeDigitsOnly(registeredCarrierNumber.getText().toString());
        }
        return getPhoneUtil().parse(carrierNumber, getSelectedCountryNameCode());
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param editTextCarrierNumber
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number= 8866667722, this will return "918866667722"
     */
    public String getFullNumber() {
        try {
            Phonenumber.PhoneNumber phoneNumber = getEnteredPhoneNumber();
            return getPhoneUtil().format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164).substring(1);
        } catch (NumberParseException e) {
            Log.e(TAG, "getFullNumber: Could not parse number");
            return getSelectedCountryCode();
        }
    }

    /**
     * Separate out country code and carrier number from fullNumber.
     * Sets country of separated country code in CCP and carrier number as text of editTextCarrierNumber
     * If no valid country code is found from full number, CCP will be set to default country code and full number will be set as carrier number to editTextCarrierNumber.
     *
     * @param fullNumber is combination of country code and carrier number, (country_code+carrier_number) for example if country is India (+91) and carrier/mobile number is 8866667722 then full number will be 9188666667722 or +918866667722. "+" in starting of number is optional.
     */
    public void setFullNumber(String fullNumber) {
        CCPCountry country = CCPCountry.getCountryForNumber(getContext(), preferredCountries, fullNumber);
        if (country == null)
            country = getDefaultCountry();
        setSelectedCountry(country);
        String carrierNumber = detectCarrierNumber(fullNumber, country);
        if (registeredCarrierNumber != null) {
            registeredCarrierNumber.setText(carrierNumber);
            updateFormattingTextWatcher();
        } else {
            Log.w(TAG, "EditText for carrier number is not registered. Register it using registerCarrierNumberEditText() before getFullNumber() or setFullNumber().");
        }
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param editTextCarrierNumber
     * This will return formatted number.
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number= 8866667722, this will return "918866667722"
     */
    public String getFormattedFullNumber() {
        try {
            Phonenumber.PhoneNumber phoneNumber = getEnteredPhoneNumber();
            return "+" + getPhoneUtil().format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL).substring(1);
        } catch (NumberParseException e) {
            Log.e(TAG, "getFullNumber: Could not parse number");
            return getSelectedCountryCode();
        }
    }

    /**
     * This function combines selected country code from CCP and carrier number from @param editTextCarrierNumber and prefix "+"
     *
     * @return Full number is countryCode + carrierNumber i.e countryCode= 91 and carrier number= 8866667722, this will return "+918866667722"
     */
    public String getFullNumberWithPlus() {
        try {
            Phonenumber.PhoneNumber phoneNumber = getEnteredPhoneNumber();
            return getPhoneUtil().format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            Log.e(TAG, "getFullNumber: Could not parse number");
            return getSelectedCountryCode();
        }
    }

    /**
     * @return content color of CCP's text and small downward arrow.
     */
    public int getContentColor() {
        return contentColor;
    }

    /**
     * Sets text and small down arrow color of CCP.
     *
     * @param contentColor color to apply to text and down arrow
     */
    public void setContentColor(int contentColor) {
        this.contentColor = contentColor;
        selectedCountry.setTextColor(this.contentColor);

        //change arrow color only if explicit arrow color is not specified.
        if (this.arrowColor == DEFAULT_UNSET) {
            arrow.setColorFilter(this.contentColor, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * set small down arrow color of CCP.
     *
     * @param arrowColor color to apply to text and down arrow
     */
    public void setArrowColor(int arrowColor) {
        this.arrowColor = arrowColor;
        if (this.arrowColor == DEFAULT_UNSET) {
            if (contentColor != DEFAULT_UNSET) {
                arrow.setColorFilter(this.contentColor, PorterDuff.Mode.SRC_IN);
            }
        } else {
            arrow.setColorFilter(this.arrowColor, PorterDuff.Mode.SRC_IN);
        }
    }

    /**
     * Sets flag border color of CCP.
     *
     * @param borderFlagColor color to apply to flag border
     */
    public void setFlagBorderColor(int borderFlagColor) {
        this.borderFlagColor = borderFlagColor;
        flagLayoutBorder.setBackgroundColor(this.borderFlagColor);
    }

    /**
     * Modifies size of text in side CCP view.
     *
     * @param textSize size of text in pixels
     */
    public void setTextSize(int textSize) {
        if (textSize > 0) {
            selectedCountry.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            setArrowSize(textSize);
            setFlagSize(textSize);
        }
    }

    /**
     * Modifies size of downArrow in CCP view
     *
     * @param arrowSize size in pixels
     */
    public void setArrowSize(int arrowSize) {
        if (arrowSize > 0) {
            LayoutParams params = (LayoutParams) arrow.getLayoutParams();
            params.width = arrowSize;
            params.height = arrowSize;
            arrow.setLayoutParams(params);
        }
    }

    /**
     * If nameCode of country in CCP view is not required use this to show/hide country name code of ccp view.
     *
     * @param showNameCode true will show country name code in ccp view, it will result " (IN) +91 "
     *                     false will remove country name code from ccp view, it will result  " +91 "
     */
    public void showNameCode(boolean showNameCode) {
        this.propertiesPicker.showNameCode(showNameCode);
        setSelectedCountry(selectedCCPCountry);
    }

    /**
     * This can change visility of arrow.
     *
     * @param showArrow true will show arrow and false will hide arrow from there.
     */
    public void showArrow(boolean showArrow) {
        propertiesPicker.showArrow(showArrow);
        refreshArrowViewVisibility();
    }

    /**
     * This will set preferred countries using their name code. Prior preferred countries will be replaced by these countries.
     * Preferred countries will be at top of country selection box.
     * If more than one countries have same country code, country in preferred list will have higher priory than others. e.g. Canada and US have +1 as their country code. If "us" is set as preferred country then US will be selected whenever setCountryForPhoneCode(1); or setFullNumber("+1xxxxxxxxx"); is called.
     *
     * @param countryPreference is country name codes separated by comma. e.g. "us,in,nz"
     */
    public void setCountryPreference(String countryPreference) {
        this.countryPreference = countryPreference;
    }

    /**
     * To change font of ccp views
     *
     * @param typeFace
     */
    public void setTypeFace(Typeface typeFace) {
        try {
            selectedCountry.setTypeface(typeFace);
            propertiesDropDown.setDropDownTypeFace(typeFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To change font of ccp views along with style.
     *
     * @param typeFace
     * @param style
     */
    public void setTypeFace(Typeface typeFace, int style) {
        try {
            selectedCountry.setTypeface(typeFace, style);
            propertiesDropDown.setDropDownTypeFace(typeFace);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To get call back on country selection a onCountryChangeListener must be registered.
     *
     * @param onCountryChangeListener
     */
    public void setOnCountryChangeListener(OnCountryChangeListener onCountryChangeListener) {
        this.onCountryChangeListener = onCountryChangeListener;
    }

    /**
     * Modifies size of flag in CCP view
     *
     * @param flagSize size in pixels
     */
    public void setFlagSize(int flagSize) {
        flag.getLayoutParams().height = flagSize;
        flag.requestLayout();
    }

    public void showFlag(boolean showFlag) {
        propertiesPicker.showFlag(showFlag);
        refreshFlagVisibility();
        if (!isInEditMode()) {
            setSelectedCountry(selectedCCPCountry);
        }
    }

    private void refreshFlagVisibility() {
        if (propertiesPicker.isShowFlag()) {
            if (propertiesDropDown.useEmoji()) {
                flagLayout.setVisibility(GONE);
            } else {
                flagLayout.setVisibility(VISIBLE);
            }
        } else {
            flagLayout.setVisibility(GONE);
        }
    }

    public void useFlagEmoji(boolean useFlagEmoji) {
        propertiesDropDown.enableEmoji(useFlagEmoji);
        refreshFlagVisibility();
        setSelectedCountry(selectedCCPCountry);
    }

    public void showFullName(boolean showFullName) {
        propertiesPicker.showFullName(showFullName);
        setSelectedCountry(selectedCCPCountry);
    }

    /**
     * Sets validity change listener.
     * First call back will be sent right away.
     *
     * @param phoneNumberValidityChangeListener
     */
    public void setPhoneNumberValidityChangeListener(PhoneNumberValidityChangeListener phoneNumberValidityChangeListener) {
        this.phoneNumberValidityChangeListener = phoneNumberValidityChangeListener;
        if (registeredCarrierNumber != null) {
            reportedValidity = isValidFullNumber();
            phoneNumberValidityChangeListener.onValidityChanged(reportedValidity);
        }
    }

    /**
     * Sets failure listener.
     *
     * @param failureListener
     */
    public void setAutoDetectionFailureListener(FailureListener failureListener) {
        this.failureListener = failureListener;
    }

    /**
     * Opens country selection dropDown.
     * By default this is called from ccp click.
     * Developer can use this to trigger manually.
     */
    public void showDropDown() {
        showDropDown(null);
    }

    /**
     * Manually trigger selection dropDown and set
     * scroll position to specified country.
     */
    public void showDropDown(final String countryNameCode) {
        DropDown dropDown = new DropDown(codePicker, countryNameCode);
        dropDown.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        dropDown.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        dropDown.setOutsideTouchable(true);
        dropDown.setFocusable(true);
        dropDown.showAsDropDown(codePicker);
    }

    /**
     * This function will check the validity of entered number.
     * It will use PhoneNumberUtil to check validity
     *
     * @return true if entered carrier number is valid else false
     */
    public boolean isValidFullNumber() {
        try {
            if (registeredCarrierNumber != null && registeredCarrierNumber.getText().length() != 0) {
                Phonenumber.PhoneNumber phoneNumber = getPhoneUtil().parse("+" + selectedCCPCountry.getPhoneCode() + registeredCarrierNumber.getText().toString(), selectedCCPCountry.getNameCode());
                return getPhoneUtil().isValidNumber(phoneNumber);
            } else if (registeredCarrierNumber == null) {
                Toast.makeText(context, "No editText for Carrier number found.", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return false;
            }
        } catch (NumberParseException e) {
            //            when number could not be parsed, its not valid
            return false;
        }
    }

    private PhoneNumberUtil getPhoneUtil() {
        if (phoneUtil == null) {
            phoneUtil = PhoneNumberUtil.getInstance();
        }
        return phoneUtil;
    }

    /**
     * loads current country in ccp using locale and telephony manager
     * this will follow specified order in countryAutoDetectionPref
     *
     * @param loadDefaultWhenFails: if all of pref methods fail to detect country then should this
     *                              function load default country or not is decided with this flag
     */
    public void setAutoDetectedCountry(boolean loadDefaultWhenFails) {
        try {
            boolean successfullyDetected = false;
            for (int i = 0; i < selectedAutoDetectionPref.representation.length(); i++) {
                switch (selectedAutoDetectionPref.representation.charAt(i)) {
                    case '1':
                        successfullyDetected = detectSIMCountry(false);
                        break;
                    case '2':
                        successfullyDetected = detectNetworkCountry(false);
                        break;
                    case '3':
                        successfullyDetected = detectLocaleCountry(false);
                        break;
                }
                if (successfullyDetected) {
                    break;
                } else {
                    if (failureListener != null) {
                        failureListener.onCountryAutoDetectionFailed();
                    }
                }
            }

            if (!successfullyDetected && loadDefaultWhenFails) {
                resetToDefaultCountry();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w(TAG, "setAutoDetectCountry: Exception" + e.getMessage());
            if (loadDefaultWhenFails) {
                resetToDefaultCountry();
            }
        }
    }

    /**
     * This will detect country from SIM info and then load it into CCP.
     *
     * @param loadDefaultWhenFails true if want to reset to default country when sim country cannot be detected. if false, then it
     *                             will not change currently selected country
     * @return true if it successfully sets country, false otherwise
     */
    public boolean detectSIMCountry(boolean loadDefaultWhenFails) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String simCountryISO = telephonyManager.getSimCountryIso();
            if (simCountryISO == null || simCountryISO.isEmpty()) {
                if (loadDefaultWhenFails) {
                    resetToDefaultCountry();
                }
                return false;
            }
            setSelectedCountry(CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), simCountryISO));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (loadDefaultWhenFails) {
                resetToDefaultCountry();
            }
            return false;
        }
    }

    /**
     * This will detect country from NETWORK info and then load it into CCP.
     *
     * @param loadDefaultWhenFails true if want to reset to default country when network country cannot be detected. if false, then it
     *                             will not change currently selected country
     * @return true if it successfully sets country, false otherwise
     */
    public boolean detectNetworkCountry(boolean loadDefaultWhenFails) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkCountryISO = telephonyManager.getNetworkCountryIso();
            if (networkCountryISO == null || networkCountryISO.isEmpty()) {
                if (loadDefaultWhenFails) {
                    resetToDefaultCountry();
                }
                return false;
            }
            setSelectedCountry(CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), networkCountryISO));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (loadDefaultWhenFails) {
                resetToDefaultCountry();
            }
            return false;
        }
    }

    /**
     * This will detect country from LOCALE info and then load it into CCP.
     *
     * @param loadDefaultWhenFails true if want to reset to default country when locale country cannot be detected. if false, then it
     *                             will not change currently selected country
     * @return true if it successfully sets country, false otherwise
     */
    public boolean detectLocaleCountry(boolean loadDefaultWhenFails) {
        try {
            String localeCountryISO = context.getResources().getConfiguration().locale.getCountry();
            if (localeCountryISO == null || localeCountryISO.isEmpty()) {
                if (loadDefaultWhenFails) {
                    resetToDefaultCountry();
                }
                return false;
            }
            setSelectedCountry(CCPCountry.getCountryForNameCodeFromLibraryMasterList(getContext(), localeCountryISO));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (loadDefaultWhenFails) {
                resetToDefaultCountry();
            }
            return false;
        }
    }

    /**
     * This will update the pref for country auto detection.
     * Remeber, this will not call setAutoDetectedCountry() to update country. This must be called separately.
     *
     * @param selectedAutoDetectionPref new detection pref
     */
    public void setCountryAutoDetectionPref(AutoDetectionPref selectedAutoDetectionPref) {
        this.selectedAutoDetectionPref = selectedAutoDetectionPref;
    }

    protected void onUserTappedCountry(CCPCountry CCPCountry) {
        if (propertiesDropDown.rememberLastSelection()) {
            codePicker.storeSelectedCountryNameCode(CCPCountry.getNameCode());
        }
        setSelectedCountry(CCPCountry);
    }

    public void setDetectCountryWithAreaCode(boolean detectCountryWithAreaCode) {
        this.detectCountryWithAreaCode = detectCountryWithAreaCode;
        updateFormattingTextWatcher();
    }

    public void setHintExampleNumberEnabled(boolean hintExampleNumberEnabled) {
        propertiesPicker.setHintExampleNumberEnabled(hintExampleNumberEnabled);
        updateHint();
    }

    public void setHintExampleNumberType(PhoneNumberType hintExampleNumberType) {
        propertiesPicker.setHintExampleNumberType(hintExampleNumberType);
        updateHint();
    }


    /**
     * To listen to the click handle action manually,
     * a custom clicklistener must be set.
     * This will override the default click listener which opens the selection dropDown.
     *
     * @param clickListener will start receiving click callbacks. If null then default click listener
     *                      will receive callback and selection dropDown will be prompted.
     */
    public void overrideClickListener(View.OnClickListener clickListener) {
        customClickListener = clickListener;
    }

    /**
     * Allows to override flags drawables for selected countries.
     * Key should represent country two-letters code;
     * Value is drawable resource,
     * For example:
     * map.put("us", R.drawable.flag_us)
     * Entries with null key or value will be ignored
     * @param customFlagsMap
     */

    public void setCustomFlagsForCountries(Map<String, Integer> customFlagsMap) {
        Map<String, Integer> mapWithUpperCaseCodes = new HashMap<>();
        boolean isSelectedCountryAffected  = false;
        for (Map.Entry<String, Integer> codeFlagResEntry : customFlagsMap.entrySet()) {
            String countryCode = codeFlagResEntry.getKey();
            Integer flagResourceId = codeFlagResEntry.getValue();

            if (countryCode != null && countryCode.length() == 2
                    && flagResourceId != null) {
                countryCode = countryCode.toUpperCase();
                mapWithUpperCaseCodes.put(countryCode,
                        flagResourceId);

                if(selectedCCPCountry != null && selectedCCPCountry.getNameCode().equals(countryCode)){
                    isSelectedCountryAffected = true;
                }
            }
        }

        this.customFlagsMap = mapWithUpperCaseCodes;

        refreshCustomMasterList();

        if(isSelectedCountryAffected){
            setSelectedCountry(selectedCCPCountry);
        }
    }

    public enum AutoDetectionPref {
        SIM_ONLY("1"),
        NETWORK_ONLY("2"),
        LOCALE_ONLY("3"),
        SIM_NETWORK("12"),
        NETWORK_SIM("21"),
        SIM_LOCALE("13"),
        LOCALE_SIM("31"),
        NETWORK_LOCALE("23"),
        LOCALE_NETWORK("32"),
        SIM_NETWORK_LOCALE("123"),
        SIM_LOCALE_NETWORK("132"),
        NETWORK_SIM_LOCALE("213"),
        NETWORK_LOCALE_SIM("231"),
        LOCALE_SIM_NETWORK("312"),
        LOCALE_NETWORK_SIM("321");

        String representation;

        AutoDetectionPref(String representation) {
            this.representation = representation;
        }

        public static AutoDetectionPref getPrefForValue(String value) {
            for (AutoDetectionPref autoDetectionPref : AutoDetectionPref.values()) {
                if (autoDetectionPref.representation.equals(value)) {
                    return autoDetectionPref;
                }
            }
            return SIM_NETWORK_LOCALE;
        }
    }
}
