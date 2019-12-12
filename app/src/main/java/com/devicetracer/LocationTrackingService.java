package com.devicetracer;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class LocationTrackingService extends Service {

	private static final String TAG = LocationTrackingService.class.getSimpleName();
	private static final int NOTIFICATION_ID = 123;
	private FusedLocationProviderClient mFusedLocationClient;
	private LocationCallback mLocationCallback;
	private Location mLocation;
	private NotificationManager mNotificationManager;
	private LocationRequest mLocationRequest;
	private Handler mServiceHandler;
	private IBinder mBinder = new LocalBinder();
	private boolean mChangingConfiguration = false;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

	public LocationTrackingService() {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mLocationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				super.onLocationResult(locationResult);
				onNewLocation(locationResult.getLastLocation());
			}
		};
		createLocationRequest();
		getLastLocation();

		HandlerThread handlerThread = new HandlerThread(TAG);
		handlerThread.start();
		mServiceHandler = new Handler(handlerThread.getLooper());
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();
	}

	private void getLastLocation() {
		try {
			mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
				@Override
				public void onComplete(@NonNull Task<Location> task) {
					if(task.isSuccessful() && task.getResult()!=null) {
						mLocation = task.getResult();
					}
				}
			});
		} catch (Exception ex) {
			//Lost Location Permission
		}
	}

	private void createLocationRequest() {
		mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10000);
		mLocationRequest.setFastestInterval(5000);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	}

	private void onNewLocation(Location location) {
		mLocation = location;

		if(mAuth.getCurrentUser()!=null) {
			uploadDeviceLocation(location);
		}

		if(serviceIsRunningInForeground(this)) {
			mNotificationManager.notify(NOTIFICATION_ID, getNotification());
		}
	}

	private void uploadDeviceLocation(Location location) {
		try {
			mDatabase.getReference("Devices").child(Utility.deviceImei(getApplicationContext())).setValue(new DeviceData(location.getLatitude(), location.getLongitude(), location.getTime()));
		} catch (Exception ex) {
			//Failed to upload device location.
		}
	}

	private Notification getNotification() {
		Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_ID)
				.setContentTitle("Location tracking turned on")
				.setContentText("Logout from the app to stop tracking")
				.setOngoing(true)
				.setPriority(Notification.PRIORITY_HIGH)
				.setSmallIcon(R.drawable.ic_logo)
				.setWhen(System.currentTimeMillis())
				.build();

		return notification;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		stopForeground(true);
		mChangingConfiguration = false;
		return mBinder;
	}

	@Override
	public void onRebind(Intent intent) {
		stopForeground(true);
		mChangingConfiguration = false;
		super.onRebind(intent);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if(!mChangingConfiguration) {
			startForeground(NOTIFICATION_ID, getNotification());
		}
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mServiceHandler.removeCallbacksAndMessages(null);
	}

	public boolean serviceIsRunningInForeground(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (getClass().getName().equals(service.service.getClassName())) {
				if (service.foreground) {
					return true;
				}
			}
		}
		return false;
	}

	public void requestLocationUpdate() {
		startService(new Intent(getApplicationContext(), LocationTrackingService.class));
		try {
			mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
		} catch (Exception ex) {
			//Lost request permission
		}
	}

	public void removeLocationUpdate() {
		try {
			mFusedLocationClient.removeLocationUpdates(mLocationCallback);
			stopSelf();
		} catch (Exception ex) {
			//Lost location permission. Could not remove update
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mChangingConfiguration = true;
	}

	public class LocalBinder extends Binder {
		LocationTrackingService getService() {
			return LocationTrackingService.this;
		}
	}
}
