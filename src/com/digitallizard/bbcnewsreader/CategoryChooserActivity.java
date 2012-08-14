/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class CategoryChooserActivity extends SherlockFragmentActivity {
	/* constants */
	public static final String KEY_CATEGORY_BOOLEANS = "categorybooleans";
	
	/* variables */
	String[] allCategoryNames;
	ListView listView;
	Button saveButton;
	
	void saveCategoriesAndReturn() {
		// send the category state back to the main activity where it will be saved
		Intent result = new Intent();
		boolean[] booleans = new boolean[allCategoryNames.length]; // stores the checked booleans
		// loop through to set booleans
		for (int i = 0; i < allCategoryNames.length; i++) {
			booleans[i] = listView.isItemChecked(i);
		}
		result.putExtra("categorybooleans", booleans);
		setResult(RESULT_OK, result);
		this.finish(); // end the activity
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// inflate the menu
		getSupportMenuInflater().inflate(R.menu.category_chooser_menu, menu);
		return true; // we have made the menu so we can return true
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.categoryChooserMenuItemSave) {
			// save the categories and exit
			saveCategoriesAndReturn();
			return true;
		}
		else if (item.getItemId() == android.R.id.home) {
			// go back without saving
			this.finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // load any saved state
		this.setContentView(R.layout.category_choice);
		
		// create a reference to the list view
		listView = (ListView) this.findViewById(R.id.categoryChoiceListView);
		
		// load the all the categories
		allCategoryNames = getResources().getStringArray(R.array.category_names); // load the full list of categories from the XML file
		listView.setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, allCategoryNames)); // load the categories into the list
		listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE); // allow multiple choices
		// load the enabled categories from the intent
		boolean[] categoryBooleans = getIntent().getBooleanArrayExtra(KEY_CATEGORY_BOOLEANS);
		// loop through enabling the categories as needed
		for (int i = 0; i < categoryBooleans.length; i++) {
			listView.setItemChecked(i, categoryBooleans[i]);
		}
	}
}
