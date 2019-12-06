package com.devicetracer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<LocationNotificationManager> notifications;

	public NotificationAdapter(Context context, ArrayList<LocationNotificationManager> notifications) {
		this.context = context;
		this.notifications = notifications;
	}

	@Override
	public int getCount() {
		return notifications.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.notification_layout, parent, false);
		}

		final CircleImageView avatar = convertView.findViewById(R.id.notification_avatar);
		final TextView message = convertView.findViewById(R.id.notification_message);
		final TextView time = convertView.findViewById(R.id.notification_time);
		final StringBuilder msg = new StringBuilder();


		FirebaseDatabase.getInstance().getReference("Users").child(notifications.get(position).getSenderID()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				if(dataSnapshot.getValue()!=null) {
					User data = dataSnapshot.getValue(User.class);
					if(data.getPhoto().length()>0) {
						Picasso.get().load(data.getPhoto()).error(R.drawable.ic_avatar).into(avatar);
					} else {
						avatar.setImageResource(R.drawable.ic_avatar);
					}

					msg.append(data.getName());

					if(notifications.get(position).getType().equals("SEEN")) {
						msg.append(" has seen your location.");
					} else {
						msg.append(" wants to see your location.");
					}

					message.setText(msg.toString());
					time.setText(Utility.realTime(notifications.get(position).getTime()));
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {

			}
		});

		return convertView;
	}
}
