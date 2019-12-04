package com.devicetracer;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener{
	public static Activity activity;

	private Button _regBtn, _loginBtn;
	private CardView cardHead;

	private PermissionManager mPermission;
	private String[] mPerms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_welcome);
		this.activity = this;

		mPerms = new String[] {
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};
		mPermission = new PermissionManager(this);
		mPermission.requestPermission(mPerms);

		_regBtn     = findViewById(R.id.welcome_regBtn);
		_loginBtn   = findViewById(R.id.welcome_loginBtn);

		_regBtn.setOnClickListener(this);
		_loginBtn.setOnClickListener(this);

		cardHead = findViewById(R.id.welcome_cardHead);
		cardHead.setBackgroundResource(R.drawable.bg_light_cardhead);

	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	public void onClick(View v) {
		if(!mPermission.hasPermission()) {
			mPermission.requestPermission(mPerms);
			return;
		}

		if(v == _regBtn) {
			Intent _registrationScreen = new Intent(getApplicationContext(), RegistrationActivity.class);
			startActivity(_registrationScreen);
		} else if(v == _loginBtn) {
			Intent _loginScreen = new Intent(getApplicationContext(), LoginActivity.class);
			startActivity(_loginScreen);
		}
	}
}
