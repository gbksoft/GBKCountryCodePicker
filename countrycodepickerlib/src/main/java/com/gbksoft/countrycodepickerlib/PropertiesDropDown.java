package com.gbksoft.countrycodepickerlib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

public class PropertiesDropDown {

    private boolean searchAllowed = true;
    private boolean showFlag = true;
    private boolean showNameCode = true;
    private boolean showFullCountryName = false;
    private boolean showPhoneCode = true;

    private boolean initialScrollToSelection = false;
    private boolean keyboardAutoPopup = true;

    private int backgroundColor;
    private int textColor;
    private int viewSortDefaultColor;
    private int viewSortSelectedColor;

    private int searchEditTextTintColor;

    private Typeface dropDownTypeFace;
    private int dropDownTypeFaceStyle;

    private boolean useEmoji = false;
    private boolean useDummyEmojiForPreview = false;

    private boolean rememberLastSelection = false;

    private DropDownEventsListener dropDownEventsListener;

    public PropertiesDropDown() {

    }

    void init(Context context, TypedArray a) {
        searchAllowed = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_allowSearch, true);
        //show flag on dropDown
        showFlag = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_showFlag, true);
        //show name code on dropDown
        showNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_showNameCode, true);
        //show name code on dropDown
        showNameCode = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_showNameCode, true);
        //show phone code on dropDown
        showFullCountryName = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_showFullCountryName, false);
        //show phone code on dropDown
        showPhoneCode = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_showPhoneCode, true);

        //dropDown colors
        backgroundColor = a.getColor(R.styleable.CountryCodePicker_ccpDropDown_backgroundColor, context.getResources().getColor(android.R.color.white));
        textColor = a.getColor(R.styleable.CountryCodePicker_ccpDropDown_textColor, 0);
        viewSortDefaultColor = a.getColor(R.styleable.CountryCodePicker_ccpDropDown_viewSortDefaultColor,
                ContextCompat.getColor(context, android.R.color.black));
        viewSortSelectedColor = a.getColor(R.styleable.CountryCodePicker_ccpDropDown_viewSortSelectedColor,
                ContextCompat.getColor(context, android.R.color.holo_red_dark));
        searchEditTextTintColor = a.getColor(R.styleable.CountryCodePicker_ccpDropDown_searchEditTextTint, 0);

        //dropDown initial scroll to selection
        initialScrollToSelection = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_initialScrollToSelection, false);
        //autopop keyboard
        keyboardAutoPopup = a.getBoolean(R.styleable.CountryCodePicker_ccpDropDown_keyboardAutoPopup, true);

        //use OS emojis instead of PNGs
        useEmoji = a.getBoolean(R.styleable.CountryCodePicker_ccp_useFlagEmoji, false);
        //use OS emojis for preview
        useDummyEmojiForPreview = a.getBoolean(R.styleable.CountryCodePicker_ccp_useDummyEmojiForPreview, false);

        //remember last selection
        rememberLastSelection = a.getBoolean(R.styleable.CountryCodePicker_ccp_rememberLastSelection, false);
    }


    //========= Boolean flags ======================================================================

    /**
     * SelectionSearch is the facility to search through the list of country while selecting.
     *
     * @return true if search is set allowed
     */
    public boolean isSearchAllowed() {
        return searchAllowed;
    }

    /**
     * SelectionSearch is the facility to search through the list of country while selecting.
     *
     * @param searchAllowed true will allow search and false will hide search box
     */
    public void setSearchAllowed(boolean searchAllowed) {
        this.searchAllowed = searchAllowed;
    }

    /**
     * To show/hide flag from country selection dropDown
     */
    public boolean isShowFlag() {
        return this.showFlag;
    }

    /**
     * To show/hide flag from country selection dropDown
     *
     * @param showFlag
     */
    public void setShowFlag(boolean showFlag) {
        this.showFlag = showFlag;
    }

    /**
     * To show/hide name code from country selection dropDown
     */
    public boolean isShowNameCode() {
        return this.showNameCode;
    }

    /**
     * To show/hide name code from country selection dropDown
     *
     * @param showNameCode
     */
    public void setShowNameCode(boolean showNameCode) {
        this.showNameCode = showNameCode;
    }

    public boolean isShowFullCountryName() {
        return showFullCountryName;
    }

    public void setShowFullCountryName(boolean showFullCountryName) {
        this.showFullCountryName = showFullCountryName;
    }

    public boolean isInitialScrollToSelectionEnabled() {
        return initialScrollToSelection;
    }

    public boolean isShowPhoneCode() {
        return showPhoneCode;
    }

    /**
     * To show/hide phone code from country selection dropDown
     *
     * @param showPhoneCode
     */
    public void setShowPhoneCode(boolean showPhoneCode) {
        this.showPhoneCode = showPhoneCode;
    }

    //========= Colors =============================================================================

    public int getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * This will be color of dropDown background
     *
     * @param backgroundColor
     */
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getSearchEditTextTintColor() {
        return searchEditTextTintColor;
    }

    /**
     * If device is running above or equal LOLLIPOP version, this will change tint of search edittext background.
     *
     * @param searchEditTextTintColor
     */
    public void setSearchEditTextTintColor(int searchEditTextTintColor) {
        this.searchEditTextTintColor = searchEditTextTintColor;
    }

    public int getTextColor() {
        return textColor;
    }

    /**
     * This color will be applied to
     * Name of country
     * Phone code of country
     * "X" button to clear query
     * preferred country divider if preferred countries defined (semi transparent)
     *
     * @param textColor
     */
    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    //======= TypeFace =============================================================================

    int getDropDownTypeFaceStyle() {
        return dropDownTypeFaceStyle;
    }

    /**
     * Publicly available functions from library
     */

    Typeface getDropDownTypeFace() {
        return dropDownTypeFace;
    }

    /**
     * To change font of ccp views
     *
     * @param typeFace
     */
    public void setDropDownTypeFace(Typeface typeFace) {
        try {
            dropDownTypeFace = typeFace;
            dropDownTypeFaceStyle = CountryCodePicker.DEFAULT_UNSET;
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
    public void setDropDownTypeFace(Typeface typeFace, int style) {
        try {
            dropDownTypeFace = typeFace;
            if (dropDownTypeFace == null) {
                style = CountryCodePicker.DEFAULT_UNSET;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getViewSortDefaultColor() {
        return viewSortDefaultColor;
    }
    /**
     * To change text and arrow default color of sort button
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * {@link ContextCompat#getColor(Context, int) getColor}.
     */
    public void setViewSortDefaultColor(@ColorInt int color) {
        this.viewSortDefaultColor = color;
    }

    public int getViewSortSelectedColor() {
        return viewSortSelectedColor;
    }


    /**
     * To change text and arrow selected color of sort button
     * @param color A color value in the form 0xAARRGGBB.
     * Do not pass a resource ID. To get a color value from a resource ID, call
     * {@link ContextCompat#getColor(Context, int) getColor}.
     */
    public void setViewSortSelectedColor(@ColorInt int color) {
        this.viewSortSelectedColor = color;
    }

    //======= Emoji ================================================================================

    public void enableEmoji(boolean useEmoji) {
        this.useEmoji = useEmoji;
    }

    public void enableDummyEmojiForPreview(boolean useDummyEmojiForPreview) {
        this.useDummyEmojiForPreview = useDummyEmojiForPreview;
    }

    public boolean useEmoji() {
        return useEmoji;
    }

    public boolean useDummyEmojiForPreview() {
        return useDummyEmojiForPreview;
    }

    //======= Misc =================================================================================

    /**
     * This will decide initial scroll position of countries list in dropDown.
     *
     * @param initialScrollToSelection : false -> show list without any scroll
     *                                 true -> will scroll to the position of the selected country.
     *                                 Note: if selected country is a preferred country,
     *                                 then it will not scroll and show full preferred countries list.
     */
    public void enableInitialScrollToSelection(boolean initialScrollToSelection) {
        this.initialScrollToSelection = this.initialScrollToSelection;
    }

    public boolean isKeyboardAutoPopup() {
        return keyboardAutoPopup;
    }

    /**
     * By default, keyboard pops up every time ccp is clicked and selection dropDown is opened.
     *
     * @param keyboardAutoPopup true: to open keyboard automatically when selection dropDown is opened
     *                          false: to avoid auto pop of keyboard
     */
    public void setKeyboardAutoPopup(boolean keyboardAutoPopup) {
        this.keyboardAutoPopup = keyboardAutoPopup;
    }

    public boolean rememberLastSelection() {
        return rememberLastSelection;
    }

    public DropDownEventsListener getDropDownEventsListener() {
        return dropDownEventsListener;
    }

    public void setDropDownEventsListener(DropDownEventsListener dropDownEventsListener) {
        this.dropDownEventsListener = dropDownEventsListener;
    }
}
