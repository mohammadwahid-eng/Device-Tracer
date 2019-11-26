package com.devicetracer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class SettingsFragment extends Fragment {
	private ListView options;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_settings, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		options = view.findViewById(R.id.settingsFragmentID);

		ArrayList<String> optionList = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.settings_name)));
		ArrayList<String> optionValues = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.settings_value)));

		SettingsAdapter settingsAdapter = new SettingsAdapter(getActivity().getApplicationContext(), optionList, optionValues);
		options.setAdapter(settingsAdapter);

		Switch settings_option_value = options.findViewById(R.id.settings_option_value);

		options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getActivity().getApplicationContext(), ""+ position, Toast.LENGTH_SHORT).show();
			}
		});

	}
}
