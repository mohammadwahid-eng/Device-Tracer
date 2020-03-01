package com.devicetracer;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class TraceActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener, SearchView.OnQueryTextListener {

	private GoogleMap gmap;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

	private ImageView backBtn;
	private SearchView search;
	private CardView selfTrace, syncTrace;
	private ProgressDialog progressDialog;

	private Location mLocation = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_trace);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();

		search = findViewById(R.id.trace_search);
		search.requestFocus();
		search.setOnQueryTextListener(this);

		selfTrace = findViewById(R.id.trace_self);
		syncTrace = findViewById(R.id.trace_sync);
		selfTrace.setOnClickListener(this);
		syncTrace.setOnClickListener(this);

		backBtn = findViewById(R.id.trace_backBtn);
		backBtn.setOnClickListener(this);

		progressDialog = new ProgressDialog(this, R.style.AppProgressDialog);
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage("Loading...");
		progressDialog.show();

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.search_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		gmap = googleMap;
		gmap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.gmap_style));
		gmap.setOnMarkerClickListener(this);
		showMyLocation();
	}

	private void showMyLocation() {
		String phone = mAuth.getCurrentUser().getPhoneNumber();
		if(phone!=null && phone.length() > 0) {
			findByPhone(phone);
		} else {
			findByIMEI(Utility.deviceImei(getApplicationContext()), null);
		}
	}

	private void setMarker(MarkerOptions markerOptions, User user) {
		gmap.clear();
		gmap.setTrafficEnabled(true);
		gmap.setMyLocationEnabled(true);
		gmap.setOnMarkerClickListener(this);
		gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(markerOptions.getPosition(), 15));

		Marker mMarker = gmap.addMarker(markerOptions);
		mMarker.showInfoWindow();
		if(user!=null) {
			mMarker.setTag(user);
		}
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

	@Override
	public void onClick(View v) {
		if(v == backBtn) {
			super.onBackPressed();
		} else if(v == selfTrace) {
			search.setQuery("", false);
			search.clearFocus();
			showMyLocation();
		} else if(v == syncTrace) {
			search.setQuery(search.getQuery(), true);
		}
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		query = query.trim();
		progressDialog.setMessage("Searching...");
		progressDialog.show();
		if(query.length() == 15) {
			findByIMEI(query, null);
		} else {
			findByPhone(query);
		}
		return false;
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if(newText.trim().length()==0) {
			showMyLocation();
		}
		return false;
	}

	private void findByIMEI(String imei, final User user) {

		mDatabase.getReference("Devices").child(imei).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				progressDialog.hide();
				if(dataSnapshot.getValue()!=null) {
					DeviceData data = dataSnapshot.getValue(DeviceData.class);
					MarkerOptions markerOptions = new MarkerOptions();

					if(user!=null) {
						markerOptions.title(user.getName());
						markerOptions.snippet("Last online: " + Utility.realTime(data.getTime()));
						markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(Uri.parse(user.getPhoto()))));
					} else {
						markerOptions.title("Last online: " + Utility.realTime(data.getTime()));
						markerOptions.icon(BitmapDescriptorFactory.fromBitmap(mapMarker(null)));
					}

					markerOptions.position(new LatLng(data.getLatitude(), data.getLongitude()));
					setMarker(markerOptions, user);

				} else {
					Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	private void findByPhone(String number) {

		mDatabase.getReference("Users").orderByChild("phone").equalTo(number).addListenerForSingleValueEvent(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				progressDialog.hide();
				if(dataSnapshot.getValue()!=null) {
					for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
						User userData = snapshot.getValue(User.class);
						findByIMEI(userData.getImei(), userData);

						if(!userData.getUid().equals(mAuth.getUid())) {
							final DatabaseReference dbRef = mDatabase.getReference("Users").child(userData.getUid()).child("notifications");
							dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
								@Override
								public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
									LocationNotificationManager notification = new LocationNotificationManager(
											dataSnapshot.getChildrenCount(),
											mAuth.getUid(),
											"SEEN",
											System.currentTimeMillis(),
											false
									);

									dbRef.child(String.valueOf(dataSnapshot.getChildrenCount())).setValue(notification);
								}

								@Override
								public void onCancelled(@NonNull DatabaseError databaseError) {

								}
							});
						}
					}
				} else {
					Toast.makeText(getApplicationContext(), "No device found", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		System.out.println("Clicked");
		User user = (User) marker.getTag();
		if(user!=null) {
			System.out.println(user.getImei());
		}
		return false;
	}
}
