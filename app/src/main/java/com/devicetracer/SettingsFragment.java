package com.devicetracer;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

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

		SettingsAdapter settingsAdapter = new SettingsAdapter(getActivity().getApplicationContext(), optionList);
		options.setAdapter(settingsAdapter);
	}
}
