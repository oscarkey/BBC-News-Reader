/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class CategoryActivity extends Activity {
	//HashMap<String, Integer> itemIds;
	ListView listView;
	DatabaseHandler database;
	ArrayList<NewsItem> items;
	
	public void onDestroy(){
		//shutdown the database
		database.onDestroy();
		
		super.onDestroy();
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //create the parent class
		this.setContentView(R.layout.category); //load the layout
				
		listView = (ListView)this.findViewById(R.id.categoryListView);
		
		//set the title of this category
		String title = this.getIntent().getStringExtra("title"); //load title from the intent
		((TextView)findViewById(R.id.categoryTitle)).setText(title);
		
		//load in news items
		database = new DatabaseHandler(this, 0); //clear old date doesn't matter here
		items = new ArrayList<NewsItem>(Arrays.asList(database.getItems(title, 50))); //specify a high limit for the number of items
		listView.setAdapter(new ItemAdapter(this, R.layout.category_item, items));
		listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		
		//add a listener to detect clicks
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//retrieve the NewsItem at the index of the click
				NewsItem item = (NewsItem)parent.getAdapter().getItem(position);
				//launch an activity to view this item
				Intent intent = new Intent(parent.getContext(), ArticleActivity.class);
		    	intent.putExtra("id", item.getId());
		    	startActivity(intent);
			}
		});
	}
}
