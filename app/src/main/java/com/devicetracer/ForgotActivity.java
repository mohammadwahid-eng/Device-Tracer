package com.devicetracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class ForgotActivity extends AppCompatActivity implements View.OnClickListener {
	private ImageView _backBtn;
	private EditText _forgot_email;
	private Button _forgot_btn;
	private FirebaseAuth mAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_forgot);

		mAuth = FirebaseAuth.getInstance();

		_backBtn = findViewById(R.id.forgot_backBtn);
		_forgot_email = findViewById(R.id.forgot_email);
		_forgot_btn = findViewById(R.id.forgot_btn);

		_backBtn.setOnClickListener(this);
		_forgot_btn.setOnClickListener(this);

		progressDialog = new ProgressDialog(ForgotActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

	}

	@Override
	public void onClick(View v) {
		if(v == _backBtn) {
			this.finish();
		} else if(v == _forgot_btn) {
			String email = _forgot_email.getText().toString().trim();

			if(email.equals("")) {
				_forgot_email.setError("Email is required.");
				_forgot_email.requestFocus();
				return;
			}

			if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				_forgot_email.setError("Please enter valid email address.");
				_forgot_email.requestFocus();
				return;
			}

			progressDialog.show();
			mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					progressDialog.hide();
					if (task.isSuccessful()) {
						finish();
						Intent _resetScreen = new Intent(getApplicationContext(), ResetActivity.class);
						startActivity(_resetScreen);
					} else {
						if(task.getException() instanceof FirebaseAuthInvalidUserException) {
							Toast.makeText(getApplicationContext(), "Email does not exist in our database.", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			});
		}
	}
}
