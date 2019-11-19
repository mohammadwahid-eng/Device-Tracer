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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

	private ImageView _backBtn;
	private EditText _login_email, _login_password;
	private Button _login_btn;
	private TextView _login_forgotBtn;
	private FirebaseAuth mAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_login);

		mAuth = FirebaseAuth.getInstance();

		_backBtn = findViewById(R.id.login_backBtn);
		_backBtn.setOnClickListener(this);

		_login_email        = findViewById(R.id.login_email);
		_login_password     = findViewById(R.id.login_password);
		_login_forgotBtn    = findViewById(R.id.login_forgotBtn);
		_login_btn          = findViewById(R.id.login_btn);

		progressDialog = new ProgressDialog(LoginActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

		_login_forgotBtn.setOnClickListener(this);
		_login_btn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == _backBtn) {
			this.finish();
		} else if(v == _login_forgotBtn) {
			Intent _forgotScreen = new Intent(getApplicationContext(), ForgotActivity.class);
			startActivity(_forgotScreen);
		} else if(v == _login_btn) {
			String email = _login_email.getText().toString().trim();
			String password = _login_password.getText().toString().trim();

			if(email.equals("")) {
				_login_email.setError("Email is required.");
				_login_email.requestFocus();
				return;
			}

			if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				_login_email.setError("Please enter valid email address.");
				_login_email.requestFocus();
				return;
			}

			if(password.equals("")) {
				_login_password.setError("Password is required.");
				_login_password.requestFocus();
				return;
			}

			progressDialog.show();
			mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					progressDialog.hide();
					if (task.isSuccessful()) {
						finish();
						Intent _dashboardScreen = new Intent(getApplicationContext(), DashboardActivity.class);
						startActivity(_dashboardScreen);
					} else {
						Toast.makeText(getApplicationContext(), "Email/Password didn't matched.", Toast.LENGTH_SHORT).show();
					}
				}
			});

		}
	}
}
