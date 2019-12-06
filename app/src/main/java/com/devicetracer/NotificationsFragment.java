package com.devicetracer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NotificationsFragment extends Fragment {
	private ListView allNotification;
	private ArrayList<LocationNotificationManager> notifications;
	private NotificationAdapter adapter;

	private FirebaseAuth mAuth;
	private FirebaseDatabase mDatabase;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_notifications, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		allNotification = view.findViewById(R.id.allNotification);
		notifications = new ArrayList<LocationNotificationManager>();

		mAuth = FirebaseAuth.getInstance();
		mDatabase = FirebaseDatabase.getInstance();


		mDatabase.getReference("Users").child(mAuth.getUid()).child("notifications").addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.getValue()!=null) {
					notifications.clear();
					for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
						LocationNotificationManager notification = snapshot.getValue(LocationNotificationManager.class);
						notifications.add(notification);
					}
					adapter = new NotificationAdapter(getActivity().getApplicationContext(), notifications);
					allNotification.setAdapter(adapter);
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});
	}
}
