package com.devicetracer;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class SettingsAdapter extends BaseAdapter {
	private Context context;
	private ArrayList<String> optionList;
	public SettingsAdapter() {

	}

	public SettingsAdapter(Context context, ArrayList<String> optionList) {
		this.context = context;
		this.optionList = optionList;
	}

	@Override
	public int getCount() {
		return optionList.size();
	}

	@Override
	public Object getItem(int position) {
		return optionList.indexOf(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView==null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.settings_adapter_layout, parent, false);
		}

		TextView name = convertView.findViewById(R.id.settings_option);
		name.setText(optionList.get(position));

		return convertView;
	}
}
