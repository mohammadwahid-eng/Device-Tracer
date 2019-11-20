package com.devicetracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

	public boolean hasPermission;

	private ImageView _backBtn;
	private EditText _field_name, _field_email, _field_password;
	private Button _regBtn;

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;
	private ProgressDialog progressDialog;

	private FusedLocationProviderClient fusedLocationClient;
	private Location fLocation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_regiatration);

		permissionChecking();

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		getDeviceLocation();

		mAuth       = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();

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
	}

	@Override
	public void onClick(View v) {
		if(!hasPermission) {
			permissionChecking();
		} else {

			if(v == _backBtn) {
				this.finish();
			} else if (v == _regBtn) {

				final String name       = _field_name.getText().toString().trim();
				final String email      = _field_email.getText().toString().trim();
				final String mobile     = "";
				final String password   = _field_password.getText().toString().trim();
				final String imei       = getDeviceImei();
				final String photo      = "";

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
										//Update Device Location
										DeviceLocation deviceLocation = new DeviceLocation(fLocation.getAccuracy(), fLocation.getLatitude(), fLocation.getLongitude(), fLocation.getTime());
										DatabaseReference device = fDatabase.getReference("Devices").child(imei);
										device.setValue(deviceLocation);

										//Database entry
										DatabaseReference user = fDatabase.getReference("Users").child(mAuth.getUid());
										User userData = new User(name, mobile, imei, photo);
										user.setValue(userData);

										finish();
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

	private void getDeviceLocation() {
		fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null) {
					fLocation = location;
				}
			}
		});
	}

	private String getDeviceImei() {
		TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		String imei;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				//EasyPermission module will handle the permission
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			imei = tm.getImei();
		} else {
			imei = tm.getDeviceId();
		}
		return imei;
	}

	@AfterPermissionGranted(123)
	public void permissionChecking() {
		String[] perms = {
				Manifest.permission.INTERNET,
				Manifest.permission.ACCESS_NETWORK_STATE,
				Manifest.permission.ACCESS_COARSE_LOCATION,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.READ_EXTERNAL_STORAGE,
				Manifest.permission.WRITE_EXTERNAL_STORAGE
		};
		if (EasyPermissions.hasPermissions(this, perms)) {
			hasPermission = true;
		} else {
			hasPermission = false;
			EasyPermissions.requestPermissions(this, getString(R.string.permission_note), 123, perms);
		}
	}

	@Override
	public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

	}

	@Override
	public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
		if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
			new AppSettingsDialog.Builder(this).build().show();
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}
}
