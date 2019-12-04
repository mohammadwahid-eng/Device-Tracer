package com.devicetracer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.appcompat.app.AlertDialog;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;


public class PermissionManager {

	private boolean permission = false;
	private Context context;

	public PermissionManager() {

	}

	public PermissionManager(Context context) {
		this.context = context;
	}

	public boolean hasPermission() {
		return permission;
	}

	public void requestPermission(String[] perms) {
		Dexter.withActivity((Activity) context)
			.withPermissions(perms)
			.withListener(new MultiplePermissionsListener() {
				@Override
				public void onPermissionsChecked(MultiplePermissionsReport report) {
					if (report.areAllPermissionsGranted()) {
						permission = true;
					}

					if (report.isAnyPermissionPermanentlyDenied()) {
						openSettingsDialog();
					}
				}

				@Override
				public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
					token.continuePermissionRequest();
				}
			})
			.onSameThread()
			.check();
	}

	private void openSettingsDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Required Permissions");
		builder.setMessage("This app require permission to use awesome feature. Grant them in app settings.");
		builder.setPositiveButton("Take Me To SETTINGS", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
				myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
				myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(myAppSettings);
			}
		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		builder.show();
	}
}
