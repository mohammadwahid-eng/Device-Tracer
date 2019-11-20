package com.devicetracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneverificationActivity extends AppCompatActivity implements View.OnClickListener {
	private ImageView _backBtn;
	private Button verifyBtn;
	private EditText otp;
	private TextView otpRequest, otp_timer;
	private String verificationID, storenumber;
	private PhoneAuthProvider.ForceResendingToken mResendToken;
	private ProgressDialog progressDialog;
	private CountDownTimer cTimer = null;

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_phoneverification);

		mAuth = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();

		_backBtn = findViewById(R.id.verify_backBtn);
		verifyBtn = findViewById(R.id.verify_btn);
		otp = findViewById(R.id.verify_otp);
		otpRequest = findViewById(R.id.verify_otp_request);

		otp_timer = findViewById(R.id.otp_timer);

		_backBtn.setOnClickListener(this);
		verifyBtn.setOnClickListener(this);
		otpRequest.setOnClickListener(this);

		progressDialog = new ProgressDialog(PhoneverificationActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

		String phoneNumber = getIntent().getStringExtra("phone_number");
		storenumber = phoneNumber;
		sendVerificationCode(phoneNumber);
	}

	private void sendVerificationCode(String number) {
		PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS, this, mCallback);
	}

	private void reSendVerificationCode() {
		Toast.makeText(getApplicationContext(), "OTP sent. Please wait.", Toast.LENGTH_LONG).show();
		PhoneAuthProvider.getInstance().verifyPhoneNumber(storenumber, 60, TimeUnit.SECONDS, this, mCallback, mResendToken);
	}

	private void countdown() {
		otp_timer.setVisibility(View.VISIBLE);
		otpRequest.setVisibility(View.INVISIBLE);
		if(cTimer!=null) {
			cTimer.cancel();
		}
		cTimer = new CountDownTimer(60000, 1000) {
			public void onTick(long millisUntilFinished) {
				otp_timer.setText("Wait: " + millisUntilFinished/1000 + "s");
			}

			public void onFinish() {
				otp_timer.setVisibility(View.GONE);
				otpRequest.setVisibility(View.VISIBLE);
			}
		};
		cTimer.start();

	}

	private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
		@Override
		public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
			super.onCodeSent(s, forceResendingToken);
			countdown();
			verificationID = s;
			mResendToken = forceResendingToken;
		}

		@Override
		public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
			String code = phoneAuthCredential.getSmsCode();
			if(code!=null) {
				verifyCode(code);
			}
		}

		@Override
		public void onVerificationFailed(FirebaseException e) {
			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
	};

	private void verifyCode(String code) {
		PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationID, code);
		verifyNumber(credential);
	}

	private void verifyNumber(PhoneAuthCredential credential) {
		progressDialog.show();
		mAuth.getCurrentUser().linkWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
			@Override
			public void onComplete(@NonNull Task<AuthResult> task) {
				progressDialog.hide();
				if(task.isSuccessful()) {
					fDatabase.getReference("Users").child(mAuth.getUid()).child("mobile").setValue(storenumber);
					finish();
					Toast.makeText(getApplicationContext(), "Number added successfully", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	public void onClick(View v) {
		if(v == _backBtn) {
			this.finish();
		} else if(v == verifyBtn) {

			String otpnum = otp.getText().toString().trim();
			if(otpnum.equals("")) {
				otp.setError("OTP is required.");
				otp.requestFocus();
				return;
			}

			if(otpnum.length() < 6) {
				otp.setError("OTP is invalid.");
				otp.requestFocus();
				return;
			}

			verifyCode(otpnum);

		} else if(v == otpRequest) {
			reSendVerificationCode();
		}
	}
}
