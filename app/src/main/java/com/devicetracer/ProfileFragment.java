package com.devicetracer;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;


import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {


	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private FirebaseStorage mStorage;

	private final int PICK_IMAGE_REQUEST = 1;

	private TextView _pname, _pemail;
	private TextInputEditText _name, _phone;
	private CircleImageView _avatar;
	private ProgressDialog progressDialog;

	private MaterialButton _passwordUpdateBtn, _bindingBtn;

	private PermissionManager mPermission;
	private String[] mPerms;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		mPerms = new String[] {
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};
		mPermission = new PermissionManager(getActivity());
		mPermission.requestPermission(mPerms);

		mAuth       = FirebaseAuth.getInstance();
		mDatabase   = FirebaseDatabase.getInstance();
		mStorage    = FirebaseStorage.getInstance();

		progressDialog = new ProgressDialog(getActivity(), R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);


		_name            = view.findViewById(R.id.profile_edit_name);
		_phone           = view.findViewById(R.id.profile_edit_phone);
		_avatar          = view.findViewById(R.id.profile_avatar);
		_pname           = view.findViewById(R.id.profile_name);
		_pemail          = view.findViewById(R.id.profile_email);
		_passwordUpdateBtn = view.findViewById(R.id.profile_edit_password);
		_bindingBtn = view.findViewById(R.id.profile_binding);


		showProfileData();

		_name.setOnClickListener(this);
		_phone.setOnClickListener(this);
		_avatar.setOnClickListener(this);
		_passwordUpdateBtn.setOnClickListener(this);
		_bindingBtn.setOnClickListener(this);
	}

	private void showProfileData() {
		mDatabase.getReference("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.getValue()!=null) {
					User currentUser = dataSnapshot.getValue(User.class);

					_pname.setText(currentUser.getName());
					_pemail.setText(currentUser.getEmail());

					_name.setText(currentUser.getName());
					_phone.setText(currentUser.getPhone());

					if(currentUser.getPhoto().length()>0) {
						Picasso.get().load(currentUser.getPhoto()).placeholder(R.drawable.ic_preloader).error(R.drawable.ic_avatar).into(_avatar);
					} else {
						_avatar.setImageResource(R.drawable.ic_avatar);
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public void onClick(View v) {
		if(!mPermission.hasPermission()) {
			mPermission.requestPermission(mPerms);
			return;
		}
		if(v == _avatar) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, PICK_IMAGE_REQUEST);

		} else if(v == _name) {
			Intent _nameScreen = new Intent(getContext(), UpdateNameActivity.class);
			startActivity(_nameScreen);
		} else if(v == _phone) {
			Intent _mobileScreen = new Intent(getContext(), PhoneActivity.class);
			startActivity(_mobileScreen);
		} else if(v == _phone) {
			Intent _mobileScreen = new Intent(getContext(), PhoneActivity.class);
			startActivity(_mobileScreen);
		} else if(v == _passwordUpdateBtn) {
			Intent _passwordScreen = new Intent(getContext(), UpdatePasswordActivity.class);
			startActivity(_passwordScreen);
		} else if(v == _bindingBtn) {
			mDatabase.getReference("Users").orderByChild("imei").equalTo(Utility.deviceImei(getActivity())).addListenerForSingleValueEvent(new ValueEventListener() {
				@Override
				public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
					if(dataSnapshot.getValue()!=null) {
						for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
							User userData = snapshot.getValue(User.class);

							Intent _bindingScreen = new Intent(getContext(), BindingActivity.class);
							_bindingScreen.putExtra("email", userData.getEmail());
							_bindingScreen.putExtra("binding_code", "" + (new Random().nextInt((999999 - 111111) + 1) + 111111));
							startActivity(_bindingScreen);
						}
					} else {
						mDatabase.getReference("Users").child(mAuth.getUid()).child("imei").setValue(Utility.deviceImei(getActivity()));
						Toast.makeText(getActivity(), "Device bounded successfully", Toast.LENGTH_SHORT).show();
					}
				}

				@Override
				public void onCancelled(@NonNull DatabaseError databaseError) {

				}
			});
		}
	}

	private void updatePicture(Uri imageURI) {
		progressDialog.show();

		try {
			Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageURI);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
			byte[] data = baos.toByteArray();

			final String filename = mAuth.getUid() + ".jpg";

			UploadTask uploadTask = mStorage.getReference("Photos").child(filename).putBytes(data);
			uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					progressDialog.hide();
					taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
						@Override
						public void onSuccess(Uri uri) {
							UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(uri).build();
							mAuth.getCurrentUser().updateProfile(profileUpdates);
							mDatabase.getReference("Users").child(mAuth.getCurrentUser().getUid()).child("photo").setValue(uri.toString());
							Toast.makeText(getContext(), "Profile picture has changed.", Toast.LENGTH_LONG).show();
						}
					});
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					progressDialog.hide();
					Toast.makeText(getContext(), "Failed. Error: "+exception.getMessage(), Toast.LENGTH_LONG).show();
				}
			}).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
					double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
					progressDialog.setMessage("Uploading " + (int) progress + "%");
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


}
