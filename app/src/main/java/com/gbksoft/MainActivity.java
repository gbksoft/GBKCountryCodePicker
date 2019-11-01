package com.gbksoft;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;

import com.gbksoft.countrycodepickerlib.CountryCodePicker;

public class MainActivity extends AppCompatActivity {

	private CountryCodePicker countryCodePicker;
	private EditText etPhone;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		bind();
	}

	private void bind() {
		countryCodePicker = (CountryCodePicker) findViewById(R.id.ccp);
		etPhone = (EditText) findViewById(R.id.et_phone);

		countryCodePicker.registerCarrierNumberEditText(etPhone);
	}
}
