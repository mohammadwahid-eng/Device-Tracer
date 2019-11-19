package com.devicetracer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.abdularis.civ.CircleImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks{

	public boolean hasPermission;

	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;
	private FirebaseStorage fStorage;

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private NavigationView navigationView;

	private FusedLocationProviderClient fusedLocationClient;

	private TextView _nav_name, _nav_email;
	private CircleImageView _nav_avatar;
	private long mBackPressed;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_dashboard);

		permissionChecking();
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		syncDeviceLocation();

		mAuth       = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();
		fStorage    = FirebaseStorage.getInstance();

		drawerLayout = findViewById(R.id.drawerLayoutID);
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
		drawerLayout.addDrawerListener(actionBarDrawerToggle);
		actionBarDrawerToggle.syncState();

		navigationView = findViewById(R.id.navigationID);
		navigationView.setNavigationItemSelectedListener(this);


		_nav_avatar = navigationView.getHeaderView(0).findViewById(R.id.navigation_avatar);
		_nav_name   = navigationView.getHeaderView(0).findViewById(R.id.navigation_name);
		_nav_email  = navigationView.getHeaderView(0).findViewById(R.id.navigation_email);

		showNavHeaderData();

		//getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new DashboardFragment()).commit();
	}

	private void showNavHeaderData() {
		fDatabase.getReference("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User data = dataSnapshot.getValue(User.class);
				_nav_name.setText(data.getName());
				_nav_email.setText(mAuth.getCurrentUser().getEmail());
				if(data.getPhoto().length()>0) {
					showProfilePicture(data.getPhoto());
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Toast.makeText(getApplicationContext(), "IMEI and Mobile number loading failed.", Toast.LENGTH_LONG).show();
			}
		});
	}

	private void showProfilePicture(String photo) {
		fStorage.getReference("Photos").child(photo).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
			@Override
			public void onSuccess(Uri uri) {
				Picasso.get().load(uri).into(_nav_avatar);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		} else if(item.getItemId() == R.id.nav_search_icon) {
			Intent _traceScreen = new Intent(getApplicationContext(), TraceActivity.class);
			startActivity(_traceScreen);
		}
		return super.onOptionsItemSelected(item);
	}

	private void shareApp() {
		Intent share = new Intent();
		share.setAction(Intent.ACTION_SEND);
		share.putExtra(Intent.EXTRA_TEXT,"Hey check out my app at: playstore url");
		share.setType("text/plain");
		startActivity(share);
	}

	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		if(item.getItemId() == R.id.nav_dashboard) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new DashboardFragment()).commit();
			this.setTitle(R.string.app_name);
		}
		else if(item.getItemId() == R.id.nav_profile) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ProfileFragment()).commit();
			this.setTitle(R.string.label_profile);
		}
		else if(item.getItemId() == R.id.nav_friends) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FriendsFragment()).commit();
			this.setTitle(R.string.label_friends);
		}
		else if(item.getItemId() == R.id.nav_notifications) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new NotificationsFragment()).commit();
			this.setTitle(R.string.label_notifications);
		}
		else if(item.getItemId() == R.id.nav_settings) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new SettingsFragment()).commit();
			this.setTitle(R.string.label_settings);
		}
		else if(item.getItemId() == R.id.nav_logout) {
			Toast.makeText(getApplicationContext(), "Log out Successfully", Toast.LENGTH_SHORT).show();
			mAuth.signOut();
			finish();
			Intent _welcomeScreen = new Intent(getApplicationContext(), WelcomeActivity.class);
			startActivity(_welcomeScreen);
		}
		else if(item.getItemId() == R.id.nav_share) {
			shareApp();
		}
		else if(item.getItemId() == R.id.nav_support) {
			sendEmail("wahid.warid55@gmail.com");
		}
		else if(item.getItemId() == R.id.nav_privacy) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new PrivacyFragment()).commit();
			this.setTitle(R.string.label_privacy);
		}
		drawerLayout.closeDrawers();
		return false;
	}

	public void sendEmail(String email) {
		Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
		startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onBackPressed() {
		if (mBackPressed + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
			return;
		}
		else {
			Toast.makeText(getBaseContext(), "Press once again to exit.", Toast.LENGTH_SHORT).show();
		}
		mBackPressed = System.currentTimeMillis();
	}

	private void getDeviceLocation() {
		fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
			@Override
			public void onSuccess(Location location) {
				if (location != null) {
					DeviceLocation deviceLocation = new DeviceLocation(location.getAccuracy(), location.getLatitude(), location.getLongitude(), location.getTime());
					DatabaseReference device = fDatabase.getReference("Devices").child(getDeviceImei());
					device.setValue(deviceLocation);
				}
			}
		});
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

	private void syncDeviceLocation() {
		getDeviceImei();
		getDeviceLocation();
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