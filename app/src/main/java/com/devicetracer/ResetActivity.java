package com.devicetracer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ResetActivity extends AppCompatActivity {
	private Button _reset_emailBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_reset);
		_reset_emailBtn = findViewById(R.id.reset_email_btn);
		_reset_emailBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
				Intent mailbox = new Intent(Intent.ACTION_MAIN);
				mailbox.addCategory(Intent.CATEGORY_APP_EMAIL);
				startActivity(mailbox);
				startActivity(Intent.createChooser(mailbox, getResources().getString(R.string.choose_email_client)));
			}
		});
	}
}
