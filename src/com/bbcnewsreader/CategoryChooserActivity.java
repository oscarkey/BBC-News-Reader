/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.bbcnewsreader;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CategoryChooserActivity extends ListActivity {
	/* constants */
	
	
	/* variables */
	String[] allCategoryNames;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		//a key has been pressed
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
	    	//it was the back key
	    	//send the category state back to the main activity where it will be saved
			Intent result = new Intent();
			boolean[] booleans = new boolean[allCategoryNames.length]; //stores the checked booleans
			//loop through to set booleans
			for(int i = 0; i < allCategoryNames.length; i++){
				booleans[i] = getListView().isItemChecked(i);
			}
			result.putExtra("categorybooleans", booleans);
			setResult(RESULT_OK, result);
			finish(); //end the activity
	        return true; //we have used to key press
	    }
	    //we don't know what to do, lets hope the super does
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load any saved state
		//add a header view to tell the user what to do
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE); //layout inflater to make an object from XML
		View header = inflater.inflate(R.layout.category_choice_header,	null); //the header
		getListView().addHeaderView(header); //set the header view to the header of the list
		//load the all the categories
		allCategoryNames = getResources().getStringArray(R.array.category_names); //load the full list of categories from the XML file
		getListView().setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, allCategoryNames)); //load the categories into the list
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //allow multiple choices
		//load the enabled categories from the intent
		boolean[] categoryBooleans = getIntent().getBooleanArrayExtra("categorybooleans");
		//loop through enabling the categories as needed
		for(int i = 0; i < categoryBooleans.length; i++){
			getListView().setItemChecked(i + 1, categoryBooleans[i]);
		}
	}
}
