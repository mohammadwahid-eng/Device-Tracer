package com.devicetracer;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class TraceActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {

	public boolean hasPermission;

	private GoogleMap gmap;
	private MarkerOptions marker;
	private LatLng geoCord;

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;
	private FirebaseStorage fStorage;
	private FusedLocationProviderClient fusedLocationClient;

	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace);
		permissionChecking();

		mAuth = FirebaseAuth.getInstance();
		fDatabase = FirebaseDatabase.getInstance();
		fStorage    = FirebaseStorage.getInstance();

		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

		progressDialog = new ProgressDialog(this, R.style.AppProgressDialog);
		progressDialog.setCanceledOnTouchOutside(false);

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);

		SearchView search = (SearchView) menu.findItem(R.id.nav_search).getActionView();
		search.setMaxWidth(Integer.MAX_VALUE);
		search.setQueryHint(getString(R.string.search_hints));
		search.setInputType(InputType.TYPE_CLASS_NUMBER);
		search.onActionViewExpanded();

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		progressDialog.setMessage("Loading");
		progressDialog.show();
		gmap = googleMap;

		fDatabase.getReference("Devices").child(getDeviceImei()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				DeviceLocation deviceData = dataSnapshot.getValue(DeviceLocation.class);
				setMarker(deviceData);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				progressDialog.hide();
				Toast.makeText(getApplicationContext(), "Error: "+databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void setMarker(DeviceLocation deviceData) {
		gmap.clear();
		geoCord = new LatLng(deviceData.getLatitude(), deviceData.getLongitude());
		marker = new MarkerOptions();
		marker.position(geoCord);
		setMarkerPhoto();
	}

	private void setMarkerPhoto() {
		fDatabase.getReference("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User userData = dataSnapshot.getValue(User.class);
				marker.title(userData.getName());
				marker.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(null)));

				if(userData.getPhoto().length()>0) {
					getMarkerPhoto(userData.getPhoto());
				} else {
					gmap.addMarker(marker);
					gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoCord, 15));
					progressDialog.hide();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				progressDialog.hide();
				Toast.makeText(getApplicationContext(), "Error: "+databaseError.getMessage(), Toast.LENGTH_LONG).show();
			}
		});
	}

	private void getMarkerPhoto(String photo) {
		fStorage.getReference("Photos").child(photo).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
			@Override
			public void onSuccess(Uri uri) {
				marker.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(uri)));
				gmap.addMarker(marker);
				gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoCord, 15));
				progressDialog.hide();
			}
		});
	}

	private Bitmap mapMarker(Uri uri) {
		View markerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker, null);

		ImageView markerIcon = markerView.findViewById(R.id.gmarker);
		CircleImageView markerPhoto = markerView.findViewById(R.id.mphoto);

		markerIcon.setImageResource(R.drawable.ic_marker);
		Picasso.get().load(uri).placeholder(R.drawable.ic_avatar).into(markerPhoto);

		markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
		markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
		markerView.buildDrawingCache();
		Bitmap marker = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(marker);
		canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
		Drawable drawable = markerView.getBackground();
		if (drawable != null)
			drawable.draw(canvas);
		markerView.draw(canvas);
		return marker;
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
