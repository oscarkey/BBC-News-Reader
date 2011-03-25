package com.digitallizard.bbcnewsreader;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //create the super
		
		this.addPreferencesFromResource(R.xml.settings);
	}
}