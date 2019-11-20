package com.devicetracer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ResetActivity extends AppCompatActivity implements View.OnClickListener {
	private Button _reset_emailBtn, _reset_loginBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_reset);
		_reset_emailBtn = findViewById(R.id.reset_email_btn);
		_reset_loginBtn = findViewById(R.id.reset_login_btn);

		_reset_emailBtn.setOnClickListener(this);
		_reset_loginBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		if(v==_reset_emailBtn) {
			finish();
			Intent mailbox = new Intent(Intent.ACTION_MAIN);
			mailbox.addCategory(Intent.CATEGORY_APP_EMAIL);
			startActivity(mailbox);
			startActivity(Intent.createChooser(mailbox, getResources().getString(R.string.choose_email_client)));
		} else if(v==_reset_loginBtn) {
			finish();
			Intent _loginScreen = new Intent(ResetActivity.this, LoginActivity.class);
			startActivity(_loginScreen);
		}
	}
}
