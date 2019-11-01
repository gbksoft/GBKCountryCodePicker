package com.gbksoft.countrycodepickerlib;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class CountryCodeAdapter extends RecyclerView.Adapter<CountryCodeAdapter.CountryCodeViewHolder> {

    private Context context;

    private List<CCPCountry> filteredCountries;
    private List<CCPCountry> masterCountries;

    private CountryCodePicker codePicker;
    private DropDown dropDown;
    private PropertiesDropDown properties;

    private LayoutInflater inflater;
    private LinearLayout searchLayout;
    private TextView noResult;
    private EditText searchField;
    private ImageView clearQuery;

    private SortType sortType;


    CountryCodeAdapter(Context context, List<CCPCountry> countries, CountryCodePicker codePicker,
                       LinearLayout searchLayout, final EditText searchField, TextView noResult,
                       DropDown dropDown, ImageView clearQuery, SortType sortType) {
        this.context = context;
        this.masterCountries = countries;
        this.codePicker = codePicker;
        this.properties = codePicker.getPropertiesDropDown();
        this.dropDown = dropDown;
        this.noResult = noResult;
        this.searchField = searchField;
        this.searchLayout = searchLayout;
        this.clearQuery = clearQuery;
        this.inflater = LayoutInflater.from(context);
        this.sortType = sortType != null ? sortType : new SortType();
        this.filteredCountries = getFilteredCountries("");
        setSearchBar();
    }

    @Override
    public CountryCodeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View rootView = inflater.inflate(R.layout.item_country, viewGroup, false);
        return new CountryCodeViewHolder(rootView);
    }

    @Override
    public void onBindViewHolder(CountryCodeViewHolder countryCodeViewHolder, final int i) {
        countryCodeViewHolder.setCountry(filteredCountries.get(i));
        if (filteredCountries.size() > i && filteredCountries.get(i) != null) {
            countryCodeViewHolder.getMainView().setOnClickListener(view -> {
                if (filteredCountries != null && filteredCountries.size() > i) {
                    codePicker.onUserTappedCountry(filteredCountries.get(i));
                }
                if (view != null && filteredCountries != null && filteredCountries.size() > i && filteredCountries.get(i) != null) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    dropDown.dismiss();
                }
            });
        } else {
            countryCodeViewHolder.getMainView().setOnClickListener(null);
        }

    }

    @Override
    public int getItemCount() {
        return filteredCountries.size();
    }

    //==============================================================================================

    private void setSearchBar() {
        if (properties.isSearchAllowed()) {
            clearQuery.setVisibility(View.GONE);
            setTextWatcher();
            setQueryClearListener();
        } else {
            searchLayout.setVisibility(View.GONE);
        }
    }

    private void setQueryClearListener() {
        clearQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchField.setText("");
            }
        });
    }

    /**
     * add textChangeListener, to apply new query each time editText get text changed.
     */
    private void setTextWatcher() {
        if (this.searchField != null) {
            this.searchField.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    applyQuery(s.toString());
                    if (s.toString().trim().equals("")) {
                        clearQuery.setVisibility(View.GONE);
                    } else {
                        clearQuery.setVisibility(View.VISIBLE);
                    }
                }
            });

            this.searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    /**
     * Filter country list for given keyWord / query.
     * Lists all countries that contains @param query in country's name, name code or phone code.
     *
     * @param query : text to match against country name, name code or phone code
     */
    private void applyQuery(String query) {
        noResult.setVisibility(View.GONE);
        query = query.toLowerCase();

        //if query started from "+" ignore it
        if (query.length() > 0 && query.charAt(0) == '+') {
            query = query.substring(1);
        }

        filteredCountries = getFilteredCountries(query);

        if (filteredCountries.size() == 0) {
            noResult.setVisibility(View.VISIBLE);
        }
        notifyDataSetChanged();
    }

    private List<CCPCountry> getFilteredCountries(String query) {
        List<CCPCountry> tempCCPCountryList1 = new ArrayList<>();
        int preferredCountriesCount = 0;
        if (codePicker.preferredCountries != null && codePicker.preferredCountries.size() > 0) {
            for (CCPCountry CCPCountry : codePicker.preferredCountries) {
                if (CCPCountry.isEligibleForQuery(query)) {
                    tempCCPCountryList1.add(CCPCountry);
                    preferredCountriesCount++;
                }
            }
            sortList(tempCCPCountryList1);

            if (tempCCPCountryList1.size() > 0) { //means at least one preferred country is added.
                CCPCountry divider = null;
                tempCCPCountryList1.add(divider);
                preferredCountriesCount++;
            }
        }

        List<CCPCountry> tempCCPCountryList2 = new ArrayList<>();
        for (CCPCountry CCPCountry : masterCountries) {
            if (CCPCountry.isEligibleForQuery(query)) {
                tempCCPCountryList2.add(CCPCountry);
            }
        }
        sortList(tempCCPCountryList2);

        tempCCPCountryList1.addAll(tempCCPCountryList2);
        return tempCCPCountryList1;
    }

    //==============================================================================================

    private void sortList(List<CCPCountry> list) {
        Collections.sort(list, (Comparator) (o1, o2) -> {
            if (o1 != null && o2 != null) {
                if(sortType.mode == SortType.Mode.ALPHABETICAL){
                    if(sortType.order == SortType.Order.ASC){
                        return ((CCPCountry) o1).getNameCode().compareTo(((CCPCountry) o2).getNameCode()); // A_Z
                    }else if(sortType.order == SortType.Order.DESC){
                        return ((CCPCountry) o2).getNameCode().compareTo(((CCPCountry) o1).getNameCode()); // Z_A
                    }else {
                        return 0;
                    }
                }else if(sortType.mode == SortType.Mode.BY_CODE){
                    if(sortType.order == SortType.Order.ASC){
                        return ((CCPCountry) o1).getPhoneCode().compareTo(((CCPCountry) o2).getPhoneCode()); // 0_9
                    }else if(sortType.order == SortType.Order.DESC){
                        return ((CCPCountry) o2).getPhoneCode().compareTo(((CCPCountry) o1).getPhoneCode()); //9_0
                    }else{
                        return 0;
                    }
                }else{
                    return 0;
                }
            } else {
                return 0;
            }
        });
    }

    public void sort(SortType sortType) {
        this.sortType = sortType;
        applyQuery(searchField.getText().toString());
        notifyDataSetChanged();
    }
    //==============================================================================================
    public static class SortType {
        private Mode mode;
        private Order order;

        public SortType() {
            this.mode = Mode.ALPHABETICAL;
            this.order = Order.ASC;
        }

        public SortType(Mode mode, Order order) {
            this.mode = mode;
            this.order = order;
        }

        public void changeOrder(){
            order = order.selectNextValue();
        }

        public void changeMode(){
            mode = mode.selectNextValue();
        }

        public Mode getMode() {
            return mode;
        }

        public void setMode(Mode mode) {
            this.mode = mode;
        }

        public Order getOrder() {
            return order;
        }

        public Order getOppositeOrder(){
            return order.selectNextValue();
        }

        public void setOrder(Order order) {
            this.order = order;
        }

        public enum Mode {
            ALPHABETICAL,
            BY_CODE;

            public Mode selectNextValue(){
                if(this == ALPHABETICAL){
                    return BY_CODE;
                }else{
                    return ALPHABETICAL;
                }
            }
        }

        public enum Order {
            DESC,
            ASC;

            public Order selectNextValue(){
                if(this == DESC){
                    return ASC;
                }else{
                    return DESC;
                }
            }
        }
    }

    //==============================================================================================

    class CountryCodeViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout rootLayout;
        private TextView countryCode;
        private TextView phoneCode;
        private ImageView flag;
        private FrameLayout flagLayout;
        private View divider;

        CountryCodeViewHolder(View itemView) {
            super(itemView);
            rootLayout = (LinearLayout) itemView;
            countryCode = rootLayout.findViewById(R.id.countryCode);
            phoneCode = rootLayout.findViewById(R.id.phoneCode);
            flag = rootLayout.findViewById(R.id.flag);
            flagLayout = rootLayout.findViewById(R.id.flagLayout);
            divider = rootLayout.findViewById(R.id.preferenceDivider);

            if (properties.getTextColor() != 0) {
                countryCode.setTextColor(properties.getTextColor());
                phoneCode.setTextColor(properties.getTextColor());
                divider.setBackgroundColor(properties.getTextColor());
            }

            try {
                if (properties.getDropDownTypeFace() != null) {
                    if (properties.getDropDownTypeFaceStyle() != CountryCodePicker.DEFAULT_UNSET) {
                        phoneCode.setTypeface(properties.getDropDownTypeFace(), properties.getDropDownTypeFaceStyle());
                        countryCode.setTypeface(properties.getDropDownTypeFace(), properties.getDropDownTypeFaceStyle());
                    } else {
                        phoneCode.setTypeface(properties.getDropDownTypeFace());
                        countryCode.setTypeface(properties.getDropDownTypeFace());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setCountry(CCPCountry ccpCountry) {
            if (ccpCountry != null) {
                divider.setVisibility(View.GONE);
                countryCode.setVisibility(View.VISIBLE);
                phoneCode.setVisibility(View.VISIBLE);
                if (properties.isShowPhoneCode()) {
                    phoneCode.setVisibility(View.VISIBLE);
                } else {
                    phoneCode.setVisibility(View.GONE);
                }

                String countryName = "";

                if (properties.isShowFlag() && properties.useEmoji()) {
                    //extra space is just for alignment purpose
                    countryName += UtilsFlag.getEmoji(ccpCountry) + "   ";
                }

                if (properties.isShowFullCountryName()) {
                    countryName += ccpCountry.getName();
                }

                if (properties.isShowNameCode()) {
                    countryName += " (" + ccpCountry.getNameCode().toUpperCase() + ")";
                }

                countryCode.setText(countryName);
                phoneCode.setText("+" + ccpCountry.getPhoneCode());

                if (!properties.isShowFlag() || properties.useEmoji()) {
                    flagLayout.setVisibility(View.GONE);
                } else {
                    flagLayout.setVisibility(View.VISIBLE);
                    flag.setImageResource(ccpCountry.getFlagID());
                }
            } else {
                divider.setVisibility(View.VISIBLE);
                countryCode.setVisibility(View.GONE);
                phoneCode.setVisibility(View.GONE);
                flagLayout.setVisibility(View.GONE);
            }
        }

        LinearLayout getMainView() {
            return rootLayout;
        }
    }
}

