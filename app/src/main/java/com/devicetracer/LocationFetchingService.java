package com.devicetracer;
import android.Manifest;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class LocationFetchingService extends Service {

	public static final String CHANNEL_ID = "DTNOTIFICATIONCHANNEL";

	private FusedLocationProviderClient mLocationClient;
	private FirebaseAuth mAuth;
	private FirebaseDatabase fDatabase;

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = LocationServices.getFusedLocationProviderClient(this);
		mAuth = FirebaseAuth.getInstance();
		fDatabase   = FirebaseDatabase.getInstance();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(!serviceIsRunningInForeground() && mAuth.getUid()!=null) {
			notificationChannel();

			PendingIntent pendingIntent = PendingIntent.getActivity(this,0, new Intent(),0);

			Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setContentTitle("Location monitoring is on")
					.setContentText("Logout for turning off monitoring")
					.setSmallIcon(R.drawable.ic_logo)
					.setContentIntent(pendingIntent)
					.build();
			startForeground(1, notification);

			getLocationUpdate();
		}
		return START_STICKY;
	}

	private void notificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					CHANNEL_ID,"Location monitoring service",
					NotificationManager.IMPORTANCE_DEFAULT
			);

			NotificationManager manager = getSystemService(NotificationManager.class);
			manager.createNotificationChannel(serviceChannel);
		}
	}

	private void getLocationUpdate() {
		final LocationRequest locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5000);
		locationRequest.setFastestInterval(3000);
		locationRequest.setMaxWaitTime(15 * 1000);

		if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
			&& ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
			stopSelf();
			return;
		}

		mLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if(locationRequest == null) {
					return;
				}
				syncLocationDB(locationResult);
			}
		}, Looper.myLooper());

	}

	private void syncLocationDB(LocationResult locationResult) {
		DeviceLocation deviceData = new DeviceLocation();
		deviceData.setAccuracy(locationResult.getLastLocation().getAccuracy());
		deviceData.setLatitude(locationResult.getLastLocation().getLatitude());
		deviceData.setLongitude(locationResult.getLastLocation().getLongitude());
		deviceData.setTime(locationResult.getLastLocation().getTime());
		fDatabase.getReference("Devices").child(getDeviceImei()).setValue(deviceData);
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


	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		stopForeground(true);
	}

	private boolean serviceIsRunningInForeground() {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (getClass().getName().equals(service.service.getClassName())) {
				if (service.foreground) {
					return true;
				}
			}
		}
		return false;
	}
}
