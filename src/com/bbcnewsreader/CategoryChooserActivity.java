package com.bbcnewsreader;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CategoryChooserActivity extends ListActivity {
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
		//load the selected categories from the data provider
		//TODO load selected categories from the content provider
	}
	
	@Override
	public void onPause(){
		//the activity is shutting down
		super.onPause(); //tell the superclass to pause
		//save the category state to the content provider
		//TODO save state to the content provider
	}
}