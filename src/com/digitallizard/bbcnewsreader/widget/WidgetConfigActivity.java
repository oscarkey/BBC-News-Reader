/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.widget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
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
	String[] enabledCategoriesNames;
	SharedPreferences settings;
	int widgetId;
	private Dialog appNotRunDialog;
	
	void closeAppNotRunDialog() {
		appNotRunDialog = null; // destroy the dialog
		// end the program
		this.setResult(RESULT_CANCELED);
		this.finish();
	}
	
	void showAppNotRunDialog() {
		// only show the error dialog if one isn't already visible
    	if(appNotRunDialog == null){
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	builder.setMessage("Before using the widget, please first enable some categories by launching the main app.");
	    	builder.setCancelable(false);
	    	builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   closeAppNotRunDialog();
	           }
	    	});
	    	appNotRunDialog = builder.create();
	    	appNotRunDialog.show();
    	}
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.widget_config);
		widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
		
		// if the activity is cancelled
		Intent cancelResult = new Intent();
		cancelResult.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
		setResult(RESULT_CANCELED, cancelResult);
		
		// add a trigger to the save button to save the selected category
		Button saveButton = (Button)this.findViewById(R.id.widgetChoiceSaveButton);
		saveButton.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				// store the selected category
				String chosenCategory = enabledCategoriesNames[(int)listView.getCheckedItemPosition()];
				Editor editor = settings.edit();
				editor.putString(ReaderWidget.PREF_KEY_CATEGORY + widgetId, chosenCategory);
				editor.commit();
				
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
		
		// connect to the database and retrieve the enabled categories
		DatabaseHandler database = new DatabaseHandler(this);
		String[][] enabledCategories = database.getEnabledCategories();
		// if no categories were enabled, do not allow the user to proceed
		if(enabledCategories == null) {
			showAppNotRunDialog();
			return; // bail here
		}
		
		enabledCategoriesNames = enabledCategories[1];
		
		
		
		// set up the list view
		listView = (ListView)this.findViewById(R.id.widgetCategoryChoiceListView);
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, enabledCategoriesNames));
		listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		
		// find the id of the current item and enable it
		settings = getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, MODE_PRIVATE);
		String enabledCategory = settings.getString(ReaderWidget.PREF_KEY_CATEGORY + widgetId, ReaderWidget.DEFAULT_CATEGORY);
		int enabledCategoryId = 0;
		for(int i = 0; i < enabledCategoriesNames.length; i++){
			if(enabledCategoriesNames[i].equals(enabledCategory)){
				enabledCategoryId = i;
			}
		}
		listView.setItemChecked(enabledCategoryId, true);
	}
}
