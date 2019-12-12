package com.devicetracer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class BindingActivity extends AppCompatActivity implements View.OnClickListener {

	private ImageView backBtn;
	private MaterialButton bindingBtn;
	private TextInputEditText bindingCode;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private ProgressDialog progressDialog;
	private PermissionManager mPermission;
	private String[] mPerms;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_binding);

		mPerms = new String[] {
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};
		mPermission = new PermissionManager(this);
		mPermission.requestPermission(mPerms);

		mAuth       = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();

		progressDialog = new ProgressDialog(BindingActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

		backBtn = findViewById(R.id.binding_backBtn);
		bindingBtn = findViewById(R.id.binding_btn);
		bindingCode = findViewById(R.id.bindingCode);

		backBtn.setOnClickListener(this);
		bindingBtn.setOnClickListener(this);
		sendBindCodeToEmail();
	}

	@Override
	public void onClick(View v) {

		if(!mPermission.hasPermission()) {
			mPermission.requestPermission(mPerms);
			return;
		}

		if(v == backBtn) {
			finish();
		}
		if(v == bindingBtn) {
			String iCode = bindingCode.getText().toString().trim();

			if(iCode.equals("")) {
				bindingCode.setError("Binding code is required.");
				bindingCode.requestFocus();
				return;
			}
			if(iCode.equals(getIntent().getStringExtra("binding_code"))) {
				mDatabase.getReference("Users").child(mAuth.getUid()).child("imei").setValue(Utility.deviceImei(this));
				finish();
				Toast.makeText(this, "Device bounded successfully", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Wrong binding code", Toast.LENGTH_LONG).show();
			}
		}

	}

	private void sendBindCodeToEmail() {
		String email = getIntent().getStringExtra("email");
		String subject = "Device Tracer Binding Code";
		String message = "Your device binding code is: " + getIntent().getStringExtra("binding_code");

	}
}
