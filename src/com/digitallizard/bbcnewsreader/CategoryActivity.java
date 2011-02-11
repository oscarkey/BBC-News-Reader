/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class CategoryActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //load the superclass
		this.setContentView(R.layout.category); //load the layout
		
		//set the title
		((TextView)findViewById(R.id.categoryTitle)).setText(this.getIntent().getStringExtra("title"));
		//LayoutInflater inflater = new LayoutInflater
	}
}
