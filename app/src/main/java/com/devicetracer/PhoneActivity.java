package com.devicetracer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.hbb20.CountryCodePicker;

public class PhoneActivity extends AppCompatActivity implements View.OnClickListener {

	private ImageView backBtn;
	private CountryCodePicker cCode;
	private EditText phoneNumber;
	private Button phoneBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_phone);
		cCode = findViewById(R.id.phone_countryCode);
		phoneNumber = findViewById(R.id.phone_number);
		phoneBtn = findViewById(R.id.phone_btn);
		backBtn = findViewById(R.id.phone_backBtn);
		backBtn.setOnClickListener(this);
		phoneBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == backBtn) {
			this.finish();
		} else if(v == phoneBtn) {
			String country_code = cCode.getSelectedCountryCode();
			String phone_number = phoneNumber.getText().toString().trim();

			if(phone_number.equals("")) {
				phoneNumber.setError("Mobile number is required.");
				phoneNumber.requestFocus();
				return;
			}

			String full_number = "+" + country_code + phone_number;
			finish();
			Intent _verificationScreen = new Intent(PhoneActivity.this, PhoneverificationActivity.class);
			_verificationScreen.putExtra("phone_number", full_number);
			startActivity(_verificationScreen);
		}
	}
}
