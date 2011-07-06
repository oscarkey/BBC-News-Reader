package com.digitallizard.bbcnewsreader.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class WidgetConfigActivity extends Activity {
	ListView listView;
	String[] enabledCategories;
	SharedPreferences settings;
	int widgetId;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.widget_config);
		widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		
		// connect to the database and retrieve the enabled categories
		DatabaseHandler database = new DatabaseHandler(this);
		enabledCategories = database.getEnabledCategories()[1];
		
		// set up the list view
		listView = (ListView)this.findViewById(R.id.widgetCategoryChoiceListView);
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, enabledCategories));
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// find the id of the current item and enable it
		settings = getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, MODE_PRIVATE);
		String enabledCategory = settings.getString(ReaderWidget.PREF_KEY_CATEGORY + widgetId, ReaderWidget.DEFAULT_CATEGORY);
		int enabledCategoryId = 0;
		for(int i = 0; i < enabledCategories.length; i++){
			if(enabledCategories[i].equals(enabledCategory)){
				enabledCategoryId = i;
			}
		}
		listView.setItemChecked(enabledCategoryId, true);
		
		// if the activity is cancelled
		Intent cancelResult = new Intent();
		cancelResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_CANCELED, cancelResult);
		
		// add a trigger to the save button to save the selected category
		Button saveButton = (Button)this.findViewById(R.id.widgetChoiceSaveButton);
		saveButton.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				// store the selected category
				String chosenCategory = enabledCategories[(int)listView.getCheckedItemPosition()];
				Editor editor = settings.edit();
				Log.v("widget config", "putting "+chosenCategory+" into "+ReaderWidget.PREF_KEY_CATEGORY + widgetId);
				editor.putString(ReaderWidget.PREF_KEY_CATEGORY + widgetId, chosenCategory);
				
				// update the widget
				Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				int[] widgetIds = {widgetId};
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
				sendBroadcast(intent);
				
				// send a successful result
				Intent successResult = new Intent();
				successResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
				setResult(RESULT_OK, successResult);
				finish(); // end the activity and send the result
			}
		});
	}
}
