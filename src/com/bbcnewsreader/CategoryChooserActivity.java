package com.bbcnewsreader;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class CategoryChooserActivity extends ListActivity {
	String[] allCategoryNames;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		allCategoryNames = getResources().getStringArray(R.array.category_names);
		setListAdapter(new ArrayAdapter<String>(this, R.layout.category_list_item, allCategoryNames));
		//allow choices
		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
	}
}
