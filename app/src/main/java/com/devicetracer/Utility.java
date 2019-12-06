package com.devicetracer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Utility {

	static String deviceImei(Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
				//Dexter module will handle the permission
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			imei = tm.getImei();
		} else {
			imei = tm.getDeviceId();
		}
		return imei;
	}

	static String realTime(long time) {

		int SECOND_MILLIS = 1000;
		int MINUTE_MILLIS = 60 * SECOND_MILLIS;
		int HOUR_MILLIS = 60 * MINUTE_MILLIS;
		int DAY_MILLIS = 24 * HOUR_MILLIS;

		if (time < 1000000000000L) {
			time *= 1000;
		}

		long now = System.currentTimeMillis();

		if (time > now || time <= 0) {
			return null;
		}

		long diff = now - time;
		if (diff < MINUTE_MILLIS) {
			return "Just now";
		} else if (diff < 2 * MINUTE_MILLIS) {
			return "1 minute ago";
		} else if (diff < 50 * MINUTE_MILLIS) {
			return diff / MINUTE_MILLIS + " minutes ago";
		} else if (diff < 90 * MINUTE_MILLIS) {
			return "1 hour ago";
		} else if (diff < 24 * HOUR_MILLIS) {
			return diff / HOUR_MILLIS + " hours ago";
		} else if (diff < 48 * HOUR_MILLIS) {
			return "Yesterday";
		} else {
			return diff / DAY_MILLIS + " days ago";
		}
	}
}
