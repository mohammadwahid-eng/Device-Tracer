package com.devicetracer;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

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
import com.google.firebase.database.FirebaseDatabase;
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

		fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null) {
					marker = new MarkerOptions();
					geoCord = new LatLng(location.getLatitude(), location.getLongitude());
					fStorage.getReference("Photos").child(mAuth.getUid()+".jpg").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
						@Override
						public void onSuccess(Uri uri) {
							progressDialog.hide();
							marker.position(geoCord);
							marker.title("WAHID");
							marker.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(uri)));
							gmap.addMarker(marker);
							gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoCord, 15));

						}
					}).addOnFailureListener(new OnFailureListener() {
						@Override
						public void onFailure(@NonNull Exception exception) {
							progressDialog.hide();
							marker.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(null)));
							gmap.addMarker(marker);
							gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(geoCord, 15));
						}
					});
				}
			}
		});
	}

	private Bitmap mapMarker(Uri uri) {
		View markerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.map_marker, null);

		ImageView markerIcon = markerView.findViewById(R.id.gmarker);
		CircleImageView markerPhoto = markerView.findViewById(R.id.mphoto);

		markerIcon.setImageResource(R.drawable.ic_marker);
		Picasso.get().load(uri).placeholder(R.drawable.ic_avatar).error(R.drawable.ic_avatar).into(markerPhoto);

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
