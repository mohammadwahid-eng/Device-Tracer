package com.devicetracer;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment implements View.OnClickListener {

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;
	private StorageReference fStorage;

	private final int PICK_IMAGE_REQUEST = 1;

	private TextView _pname, _name, _email, _mobile, _pimei, _imei, _changeAvatar;
	private CircleImageView _avatar;
	private Button _updateBtn;
	private Uri imageURI;
	private ProgressDialog progressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mAuth       = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();
		fStorage    = FirebaseStorage.getInstance().getReference("Photos");
		progressDialog = new ProgressDialog(getActivity(), R.style.AppProgressDialog);
		progressDialog.setMessage("Processing");
		progressDialog.setCanceledOnTouchOutside(false);
		return inflater.inflate(R.layout.fragment_profile, container, false);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		_pname          = view.findViewById(R.id.profile_name);
		_name           = view.findViewById(R.id.profile_edit_name);
		_email          = view.findViewById(R.id.profile_edit_email);
		_mobile         = view.findViewById(R.id.profile_edit_mobile);
		_pimei          = view.findViewById(R.id.profile_imei);
		_imei           = view.findViewById(R.id.profile_edit_imei);
		_avatar         = view.findViewById(R.id.profile_avatar);
		_changeAvatar   = view.findViewById(R.id.profile_avatar_change);
		_updateBtn      = view.findViewById(R.id.profile_updateBtn);

		_changeAvatar.setOnClickListener(this);
		_updateBtn.setOnClickListener(this);

		final FirebaseUser user = mAuth.getCurrentUser();
		if (user != null) {
			progressDialog.show();

			// Read from the database
			FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
				@Override
				public void onDataChange(DataSnapshot dataSnapshot) {
					User data = dataSnapshot.getValue(User.class);
					showProfileData(data);
					progressDialog.hide();
				}

				@Override
				public void onCancelled(DatabaseError error) {

				}
			});
		} else {
			Toast.makeText(getContext(), "Login required.", Toast.LENGTH_SHORT).show();
		}
	}

	private void showProfileData(User data) {
		_pname.setText(data.getName());
		_pimei.setText(data.getImei());

		if(data.getPhoto().length() > 0) {
			fStorage.child(data.getPhoto()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
				@Override
				public void onSuccess(Uri uri) {
					Picasso.get().load(uri).into(_avatar);
				}
			}).addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					Toast.makeText(getContext(), "Picture loading failed.", Toast.LENGTH_SHORT).show();
				}
			});
		}

		_name.setText(data.getName());
		_email.setText(data.getEmail());
		_imei.setText(data.getImei());
		_mobile.setText(data.getMobile());
	}


	@Override
	public void onClick(View v) {
		if(v == _changeAvatar) {
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, PICK_IMAGE_REQUEST);

		} else if(v == _updateBtn) {
			final String name = _name.getText().toString().trim();
			final String email = _email.getText().toString().trim();
			final String mobile = _mobile.getText().toString().trim();
			final String imei = _imei.getText().toString().trim();

			if(name.equals("")) {
				_name.setError("Name is required.");
				_name.requestFocus();
				return;
			}

			if(email.equals("")) {
				_email.setError("Email is required.");
				_email.requestFocus();
				return;
			}

			if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
				_email.setError("Please enter valid email address.");
				_email.requestFocus();
				return;
			}

			if(mobile.equals("")) {
				_mobile.setError("Mobile number is required.");
				_mobile.requestFocus();
				return;
			}

			FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("name").setValue(name);
			FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("email").setValue(email);
			FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("mobile").setValue(mobile);
			FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid()).child("imei").setValue(imei);
			Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
		}
	}

	public static String getMimeType(Context context, Uri uri) {
		String extension;

		//Check uri format to avoid null
		if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
			//If scheme is a content
			final MimeTypeMap mime = MimeTypeMap.getSingleton();
			extension = mime.getExtensionFromMimeType(context.getContentResolver().getType(uri));
		} else {
			//If scheme is a File
			extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
		}

		return extension;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
			progressDialog.show();
			imageURI = data.getData();
			Picasso.get().load(imageURI).resize(70, 70).centerCrop().into(_avatar);

			final String filename = System.currentTimeMillis() + "." + getMimeType(getActivity().getApplicationContext(), imageURI);
			StorageReference photo = fStorage.child(filename);

			photo.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
				@Override
				public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
					fDatabase.getReference("Users").child(mAuth.getUid()).child("photo").setValue(filename);
					progressDialog.hide();
					Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
				}
			})
			.addOnFailureListener(new OnFailureListener() {
				@Override
				public void onFailure(@NonNull Exception exception) {
					Toast.makeText(getContext(), "failed.", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
