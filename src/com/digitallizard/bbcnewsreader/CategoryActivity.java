/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
		
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE); //inflater to create layouts
		
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
	
	/**public void itemClicked(View item){
		//create an intent to launch the next activity
    	Intent intent = new Intent(this, ArticleActivity.class);
    	TextView titleText = (TextView)item.findViewById(R.id.fullNewsItemName);
    	intent.putExtra("id", (int)itemIds.get(titleText.getText()));
    	startActivity(intent);
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load the superclass
		this.setContentView(R.layout.category); //load the layout
		
		itemIds = new HashMap<String, Integer>();
		
		//set the title
		String title = this.getIntent().getStringExtra("title"); //load title from the intent
		((TextView)findViewById(R.id.categoryTitle)).setText(title);
		//create an inflater to build the ui
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout scrollerContent = (LinearLayout)findViewById(R.id.categoryScrollerContent);
		
		//load in the news items from the database
		DatabaseHandler database = new DatabaseHandler(this, 0); //we don't need to bother with the clear old date
		NewsItem[] items = database.getItems(title, 50); //specify a high limit
		//check if the database was empty
		if(items != null){
			//display them
			for(int i = 0; i < items.length; i++){
				LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_full_news_item, null);
				//set the article name
				((TextView)item.findViewById(R.id.fullNewsItemName)).setText(items[i].getTitle());
				((TextView)item.findViewById(R.id.fullNewsItemDescription)).setText(items[i].getDescription());
				
				//try and get an image for this item
				byte[] imageBytes = database.getThumbnail(items[i].getId());
				//check if any image data was returned
				if(imageBytes != null){
					//try to construct an image out of the bytes given by the database
					Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); //load the image into a bitmap
					ImageView imageView = (ImageView)item.findViewById(R.id.fullNewsItemImage);
					imageView.setImageBitmap(imageBitmap);
				}
				
				scrollerContent.addView(item);
				//save the id for clicks
				itemIds.put(items[i].getTitle(), items[i].getId());
			}
		}
		database.onDestroy(); //shutdown database
	}
	**/
}
