package com.devicetracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener{

	public static Activity activity;

	private CardView cardHead;

	private ImageView _backBtn;
	private EditText _field_name, _field_email, _field_password;
	private Button _regBtn;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private ProgressDialog progressDialog;
	private PermissionManager mPermission;
	private String[] mPerms;

	private FusedLocationProviderClient fusedLocationClient;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_regiatration);
		this.activity = this;

		mPerms = new String[] {
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};
		mPermission = new PermissionManager(this);
		mPermission.requestPermission(mPerms);

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		mAuth       = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();
		mAuth.signOut();


		_backBtn = findViewById(R.id.reg_backBtn);
		_backBtn.setOnClickListener(this);

		_field_name = findViewById(R.id.reg_name);
		_field_email = findViewById(R.id.reg_email);
		_field_password = findViewById(R.id.reg_password);
		_regBtn = findViewById(R.id.reg_btn);
		_regBtn.setOnClickListener(this);

		progressDialog = new ProgressDialog(RegistrationActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

		cardHead = findViewById(R.id.reg_cardHead);
		cardHead.setBackgroundResource(R.drawable.bg_light_cardhead);
	}

	@Override
	public void onClick(View v) {
		if(!mPermission.hasPermission()) {
			mPermission.requestPermission(mPerms);
			return;
		}

		if(v == _backBtn) {
			this.finish();
		} else if (v == _regBtn) {

			final String name       = _field_name.getText().toString().trim();
			final String email      = _field_email.getText().toString().trim();
			final String password   = _field_password.getText().toString().trim();

			if(name.equals("")) {
				_field_name.setError("Name is required.");
				_field_name.requestFocus();
				return;
			}

			if(email.equals("")) {
				_field_email.setError("Email is required.");
				_field_email.requestFocus();
				return;
			}

			if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				_field_email.setError("Please enter valid email address.");
				_field_email.requestFocus();
				return;
			}

			if(password.equals("")) {
				_field_password.setError("Password is required.");
				_field_password.requestFocus();
				return;
			}

			if(password.length() < 6) {
				_field_password.setError("Password must be at least 6 characters.");
				_field_password.requestFocus();
				return;
			}

			progressDialog.show();

			mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
				@Override
				public void onComplete(@NonNull Task<AuthResult> task) {
					if (task.isSuccessful()) {
						mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								progressDialog.hide();
								if(task.isSuccessful()) {
									finish();

									mDatabase.getReference("Users").child(mAuth.getUid()).setValue(new User(mAuth.getUid(), name, "", Utility.deviceImei(getApplicationContext()), ""));
									mDatabase.getReference("Devices").child(Utility.deviceImei(activity)).setValue(new DeviceData(0, 0, 0));

									Intent _emailScreen = new Intent(RegistrationActivity.this, ResetActivity.class);
									startActivity(_emailScreen);
								} else {
									Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
								}
							}
						});

					} else {
						progressDialog.hide();
						if(task.getException() instanceof FirebaseAuthUserCollisionException) {
							Toast.makeText(getApplicationContext(), "Email address already exist.", Toast.LENGTH_SHORT).show();
						} else {
							Toast.makeText(getApplicationContext(), "Registration Failed. Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
						}
					}
				}
			});

		}
	}


}
