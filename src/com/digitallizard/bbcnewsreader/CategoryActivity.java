/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.HashMap;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CategoryActivity extends Activity {
	HashMap<String, Integer> itemIds;
	
	public void itemClicked(View item){
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
		LinearLayout scroller = (LinearLayout)findViewById(R.id.categoryScroller);
		
		//load in the news items from the database
		DatabaseHandler database = new DatabaseHandler(this, 0); //we don't need to bother with the clear old date
		String[][] items = database.getItems(title);
		//check if the database was empty
		if(items != null){
			//display them
			for(int i = 0; i < items[0].length; i++){
				LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_full_news_item, null);
				//set the article name
				((TextView)item.findViewById(R.id.fullNewsItemName)).setText(items[0][i]);
				((TextView)item.findViewById(R.id.fullNewsItemDescription)).setText(items[1][i]);
				
				//try and get an image for this item
				byte[] imageBytes = database.getThumbnail(Integer.parseInt(items[3][i]));
				//check if any image data was returned
				if(imageBytes != null){
					//try to construct an image out of the bytes given by the database
					Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); //load the image into a bitmap
					ImageView imageView = (ImageView)item.findViewById(R.id.fullNewsItemImage);
					imageView.setImageBitmap(imageBitmap);
				}
				
				scroller.addView(item);
				//save the id for clicks
				itemIds.put(items[0][i], Integer.parseInt(items[3][i]));
			}
		}
	}
}
