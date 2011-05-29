/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import com.digitallizard.bbcnewsreader.R;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CategoryChooserActivity extends Activity {
	/* constants */
	
	
	/* variables */
	String[] allCategoryNames;
	ListView listView;
	Button saveButton;
	
	void saveCategoriesAndReturn(){
		//send the category state back to the main activity where it will be saved
		Intent result = new Intent();
		boolean[] booleans = new boolean[allCategoryNames.length]; //stores the checked booleans
		//loop through to set booleans
		for(int i = 0; i < allCategoryNames.length; i++){
			booleans[i] = listView.isItemChecked(i);
		}
		result.putExtra("categorybooleans", booleans);
		setResult(RESULT_OK, result);
		this.finish(); //end the activity
	}
	
	public void saveClicked(View view){
		saveCategoriesAndReturn(); //save the categories and go back to the main activity
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
		//a key has been pressed
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	//it was the back key
	    	saveCategoriesAndReturn(); //save the categories and go back to the main activity
	    	return true; //we have used to key press
	    }
	    //we don't know what to do, lets hope the super does
	    return super.onKeyDown(keyCode, event);
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load any saved state
		this.setContentView(R.layout.category_choice);
		
		//create a reference to the list view
		listView = (ListView)this.findViewById(R.id.categoryChoiceListView);
		
		//decide if to show the save button
		
		
		//load the all the categories
		allCategoryNames = getResources().getStringArray(R.array.category_names); //load the full list of categories from the XML file
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, allCategoryNames)); //load the categories into the list
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //allow multiple choices
		//load the enabled categories from the intent
		boolean[] categoryBooleans = getIntent().getBooleanArrayExtra("categorybooleans");
		//loop through enabling the categories as needed
		for(int i = 0; i < categoryBooleans.length; i++){
			listView.setItemChecked(i, categoryBooleans[i]);
		}
	}
}
