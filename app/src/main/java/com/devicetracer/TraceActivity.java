package com.devicetracer;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TraceActivity extends AppCompatActivity implements OnMapReadyCallback{
	private GoogleMap gmap;
	private FirebaseAuth mAuth;
	private DatabaseReference mDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trace);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance().getReference("Users");

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		showMyLocation();

	}

	private void showMyLocation() {
		mDatabase.child(mAuth.getUid()).child("location").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				DeviceData data = dataSnapshot.getValue(DeviceData.class);
				//LatLng location = new LatLng(data.getLatitude(), data.getLongitude());
				//loadMap(location);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
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
		gmap = googleMap;
	}

	public void loadMap(LatLng position) {
		gmap.clear();
		gmap.addMarker(new MarkerOptions().position(position).title("Marker Title"));
		gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
	}
}
