package com.devicetracer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class UpdateNameActivity extends AppCompatActivity implements View.OnClickListener {
	private TextInputEditText updateName;
	private MaterialButton updateBtn;
	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private ImageView _backBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_name_change);

		mAuth       = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();

		_backBtn = findViewById(R.id.update_name_backBtn);
		_backBtn.setOnClickListener(this);

		updateName = findViewById(R.id.update_name);
		updateBtn = findViewById(R.id.update_name_btn);
		updateBtn.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		if(v==_backBtn) {
			this.finish();
		} else if(v==updateBtn) {
			String name = updateName.getText().toString().trim();
			if(name.equals("")) {
				updateName.setError("Name is required.");
				updateName.requestFocus();
				return;
			}
			mDatabase.getReference("Users").child(mAuth.getUid()).child("name").setValue(name);
			Toast.makeText(getApplicationContext(), "Name has changed.", Toast.LENGTH_SHORT).show();
			this.finish();
		}
	}
}
