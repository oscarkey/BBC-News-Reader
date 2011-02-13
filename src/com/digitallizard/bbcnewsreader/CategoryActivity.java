/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CategoryActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load the superclass
		this.setContentView(R.layout.category); //load the layout
		
		//set the title
		String title = this.getIntent().getStringExtra("title"); //load title from the intent
		((TextView)findViewById(R.id.categoryTitle)).setText(title);
		//create an inflater to build the ui
		LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout scroller = (LinearLayout)findViewById(R.id.categoryScroller);
		
		//load in the news items from the database
		DatabaseHandler database = new DatabaseHandler(this);
		String[][] items = database.getItems(title);
		//display them
		for(int i = 0; i < items[0].length; i++){
			LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_full_news_item, null);
			scroller.addView(item);
		}
	}
}
