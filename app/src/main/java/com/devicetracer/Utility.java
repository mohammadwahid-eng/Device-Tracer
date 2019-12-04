package com.devicetracer;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class Utility {

	static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";

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

	static boolean requestingLocationUpdates(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
	}

	static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
				.apply();
	}
}
