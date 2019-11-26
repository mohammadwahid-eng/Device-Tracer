package com.devicetracer;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener, EasyPermissions.PermissionCallbacks {

	public boolean hasPermission;

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;
	private FirebaseStorage fStorage;

	private final int PICK_IMAGE_REQUEST = 1;

	private TextView _pname, _name, _mobile, _imei, _changeAvatar;
	private CircleImageView _avatar;
	private Button _updateBtn, _bindBtn;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		permissionChecking();

		mAuth       = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();
		fStorage    = FirebaseStorage.getInstance();
		progressDialog = new ProgressDialog(getActivity(), R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);

		_pname          = view.findViewById(R.id.profile_name);
		_name           = view.findViewById(R.id.profile_edit_name);
		_mobile         = view.findViewById(R.id.profile_edit_mobile);
		_imei          = view.findViewById(R.id.profile_imei);
		_avatar         = view.findViewById(R.id.profile_avatar);
		_changeAvatar   = view.findViewById(R.id.profile_avatar_change);
		_updateBtn      = view.findViewById(R.id.profile_updateBtn);
		_bindBtn      = view.findViewById(R.id.profile_bindingBtn);

		showProfileData();

		_mobile.setOnClickListener(this);
		_avatar.setOnClickListener(this);
		_changeAvatar.setOnClickListener(this);
		_updateBtn.setOnClickListener(this);
		_bindBtn.setOnClickListener(this);
	}

	private void showProfileData() {
		fDatabase.getReference("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User data = dataSnapshot.getValue(User.class);
				_pname.setText(data.getName());
				_name.setText(data.getName());
				_mobile.setText(data.getMobile());
				_imei.setText(data.getImei());
				if(data.getPhoto().length()>0) {
					showProfilePicture(data.getPhoto());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getContext(), "IMEI and Mobile number loading failed.", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void showProfilePicture(String photo) {
		fStorage.getReference("Photos").child(photo).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
			@Override
			public void onSuccess(Uri uri) {
				Picasso.get().load(uri).into(_avatar);
			}
		});
	}


	@Override
	public void onClick(View v) {
		if(v == _changeAvatar || v == _avatar) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, PICK_IMAGE_REQUEST);

		} else if(v == _mobile) {
			Intent _mobileScreen = new Intent(getContext(), PhoneActivity.class);
			startActivity(_mobileScreen);
		} else if(v == _bindBtn) {

			AlertDialog.Builder popup = new AlertDialog.Builder(getActivity());
			popup.setMessage("Do you really want to bind the device with your account?");
			popup.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fDatabase.getReference("Users").child(mAuth.getUid()).child("imei").setValue(getDeviceImei());
					Toast.makeText(getContext(), "Device binding complete.", Toast.LENGTH_SHORT).show();
				}
			});
			popup.setNegativeButton("No", null);
			AlertDialog alert = popup.create();
			alert.show();

		} else if(v == _updateBtn) {
			final String name = _name.getText().toString().trim();

			if(name.equals("")) {
				_name.setError("Name is required.");
				_name.requestFocus();
				return;
			}

			fDatabase.getReference("Users").child(mAuth.getUid()).child("name").setValue(name);
			Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
		}
	}

	private String getDeviceImei() {
		TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
		String imei;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (getActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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

	private void updatePicture(final Uri imageURI) {
		progressDialog.show();
		try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageURI);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
			byte[] data = baos.toByteArray();

			final String filename = mAuth.getUid() + ".jpg";

			UploadTask uploadTask = fStorage.getReference("Photos").child(filename).putBytes(data);
			uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					progressDialog.hide();
					fDatabase.getReference("Users").child(mAuth.getUid()).child("photo").setValue(filename);
					Toast.makeText(getContext(), "Profile picture has changed.", Toast.LENGTH_LONG).show();
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					progressDialog.hide();
					Toast.makeText(getContext(), "Failed to change profile picture. Error: "+exception.getMessage(), Toast.LENGTH_LONG).show();
				}
			});
		} catch (IOException e) {
			progressDialog.hide();
			Toast.makeText(getContext(), "Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			updatePicture(data.getData());
		}
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
				Manifest.permission.WRITE_EXTERNAL_STORAGE,
				Manifest.permission.FOREGROUND_SERVICE
		};
		if (EasyPermissions.hasPermissions(getActivity().getApplicationContext(), perms)) {
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
		EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, getActivity());
	}
}
