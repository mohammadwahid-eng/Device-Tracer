package com.devicetracer;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;
	private FirebaseStorage mStorage;

	private DrawerLayout drawerLayout;
	private ActionBarDrawerToggle actionBarDrawerToggle;
	private NavigationView navigationView;

	private TextView _nav_name, _nav_identity;
	private CircleImageView _nav_avatar;
	private long mBackPressed;
	private PermissionManager mPermission;
	private String[] mPerms;

	private LocationTrackingService mService = null;
	private boolean mBound = false;

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocationTrackingService.LocalBinder binder = (LocationTrackingService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;
			mBound = false;
		}
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_dashboard);
		mPerms = new String[] {
				Manifest.permission.READ_PHONE_STATE,
				Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.READ_EXTERNAL_STORAGE
		};
		mPermission = new PermissionManager(this);
		mPermission.requestPermission(mPerms);

		mAuth       = FirebaseAuth.getInstance();
		mDatabase   = FirebaseDatabase.getInstance();
		mStorage    = FirebaseStorage.getInstance();

		drawerLayout = findViewById(R.id.drawerLayoutID);
		actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);
		drawerLayout.addDrawerListener(actionBarDrawerToggle);
		actionBarDrawerToggle.syncState();

		navigationView = findViewById(R.id.navigationID);
		navigationView.setNavigationItemSelectedListener(this);

		_nav_avatar = navigationView.getHeaderView(0).findViewById(R.id.nav_avatar);
		_nav_name   = navigationView.getHeaderView(0).findViewById(R.id.nav_name);
		_nav_identity   = navigationView.getHeaderView(0).findViewById(R.id.nav_identity);

		showProfileData();







		getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new DashboardFragment()).commit();
	}

	@Override
	protected void onStart() {
		super.onStart();
		bindService(new Intent(this, LocationTrackingService.class), mServiceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if(mBound) {
			unbindService(mServiceConnection);
			mBound = false;
		}
	}

	private void showProfileData() {
		mDatabase.getReference("Users").child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.getValue()!=null) {
					User currentUser = dataSnapshot.getValue(User.class);

					_nav_name.setText(currentUser.getName());

					if(currentUser.getPhoto().length()>0) {
						Picasso.get().load(currentUser.getPhoto()).placeholder(R.drawable.ic_preloader).error(R.drawable.ic_avatar).into(_nav_avatar);
					} else {
						_nav_avatar.setImageResource(R.drawable.ic_avatar);
					}

					if(currentUser.getPhone().length()>0) {
						_nav_identity.setText(currentUser.getPhone());
					} else {
						_nav_identity.setText(mAuth.getCurrentUser().getEmail());
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
	public boolean onNavigationItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.nav_dashboard) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new DashboardFragment()).commit();
			this.setTitle(R.string.app_name);
		}
		else if(item.getItemId() == R.id.nav_profile) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new ProfileFragment()).commit();
			this.setTitle(R.string.label_my_profile);
		}
		else if(item.getItemId() == R.id.nav_friends) {
			getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new FriendsFragment()).commit();
			this.setTitle(R.string.label_who_can_see_me);
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

			if(mService!=null) {
				mService.removeLocationUpdate();
			}

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
}