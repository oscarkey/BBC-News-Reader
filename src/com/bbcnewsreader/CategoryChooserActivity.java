package com.bbcnewsreader;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CategoryChooserActivity extends ListActivity {
	/* constants */
	
	
	/* variables */
	String[] allCategoryNames;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load any saved state
		//add a header view to tell the user what to do
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE); //layout inflater to make an object from XML
		View header = inflater.inflate(R.layout.category_choice_header,	null); //the header
		getListView().addHeaderView(header); //set the header view to the header of the list
		//load the all the categories
		allCategoryNames = getResources().getStringArray(R.array.category_names); //load the full list of categories from the XML file
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); //allow multiple choices in the menu
		getListView().setAdapter(new ArrayAdapter<String>(this, R.layout.category_choice_item, allCategoryNames)); //load the categories into the list
		//load the enabled categories from the intent
		boolean[] categoryBooleans = getIntent().getBooleanArrayExtra("categorybooleans");
		//loop through enabling the categories as needed
		for(int i = 0; i < categoryBooleans.length; i++){
			getListView().setItemChecked(i, categoryBooleans[i]);
		}
	}
	
	@Override
	public void onPause(){
		//the activity is shutting down
		super.onPause(); //tell the superclass to pause
		//send the category state back to the main activity where it will be saved
		//TODO provide a result
	}
}