package com.devicetracer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

public class UpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener {
	private MaterialButton updateBtn;
	private TextInputEditText oldPassword, newPassword, confirmNewPassword;
	private ImageView backBtn;

	private FirebaseAuth mAuth;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_update_password);

		mAuth = FirebaseAuth.getInstance();

		backBtn = findViewById(R.id.update_password_backBtn);
		backBtn.setOnClickListener(this);

		updateBtn = findViewById(R.id.update_password_btn);
		updateBtn.setOnClickListener(this);

		oldPassword = findViewById(R.id.update_old_password);
		newPassword = findViewById(R.id.update_new_password);
		confirmNewPassword = findViewById(R.id.update_confirm_new_password);

		progressDialog = new ProgressDialog(UpdatePasswordActivity.this, R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

	}

	@Override
	public void onClick(View v) {
		if(v == backBtn) {
			this.finish();
		} else if(v==updateBtn) {
			String oldPass = oldPassword.getText().toString().trim();
			final String newPass = newPassword.getText().toString().trim();
			String confirmNewPass = confirmNewPassword.getText().toString().trim();

			if(oldPass.equals("")) {
				oldPassword.setError("Old password is required");
				oldPassword.requestFocus();
				return;
			}
			if(oldPass.length()<6) {
				oldPassword.setError("Password must be at least 6 characters");
				oldPassword.requestFocus();
				return;
			}

			if(newPass.equals("")) {
				newPassword.setError("New password is required");
				newPassword.requestFocus();
				return;
			}
			if(newPass.length()<6) {
				newPassword.setError("Password must be at least 6 characters.");
				newPassword.requestFocus();
				return;
			}

			if(confirmNewPass.equals("")) {
				confirmNewPassword.setError("Confirm password is required");
				confirmNewPassword.requestFocus();
				return;
			}

			if(!newPass.equals(confirmNewPass)) {
				confirmNewPassword.setError("Confirmation password did not matched.");
				confirmNewPassword.requestFocus();
				return;
			}

			progressDialog.show();

			AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), oldPass);
			mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
				@Override
				public void onComplete(@NonNull Task<Void> task) {
					progressDialog.hide();
					if(task.isSuccessful()) {
						mAuth.getCurrentUser().updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
							@Override
							public void onComplete(@NonNull Task<Void> task) {
								if(task.isSuccessful()) {
									finish();
									Toast.makeText(getApplicationContext(), "Password has changed", Toast.LENGTH_SHORT).show();
								} else {
									new MaterialAlertDialogBuilder(UpdatePasswordActivity.this)
										.setTitle("Password changing failed")
										.setMessage("Error: "+ task.getException().getMessage())
										.setCancelable(false)
										.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
											@Override
											public void onClick(DialogInterface dialog, int which) {
												finish();
											}
										}).show();
								}
							}
						});
					} else {
						Toast.makeText(getApplicationContext(), "Wrong old password.", Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}
}
