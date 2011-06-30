package com.digitallizard.bbcnewsreader.widget;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class WidgetConfigActivity extends Activity {
	ListView listView;
	
	public void saveClicked(View view){
		
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.widget_config);
		
		// connect to the database and retrieve the enabled categories
		DatabaseHandler database = new DatabaseHandler(this);
		String[] enabledCategories = database.getEnabledCategories()[1];
		
		// set up the list view
		listView = (ListView)this.findViewById(R.id.widgetCategoryChoiceListView);
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, enabledCategories));
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// find the id of the current item and enable it
		SharedPreferences settings = getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, MODE_PRIVATE);
		String enabledCategory = settings.getString(ReaderWidget.PREF_KEY_CATEGORY, ReaderWidget.DEFAULT_CATEGORY);
		int enabledCategoryId = 0;
		for(int i = 0; i < enabledCategories.length; i++){
			if(enabledCategories[i].equals(enabledCategory)){
				enabledCategoryId = i;
			}
		}
		listView.setItemChecked(enabledCategoryId, true);
	}
}
