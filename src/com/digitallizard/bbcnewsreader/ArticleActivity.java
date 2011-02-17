/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ArticleActivity extends Activity {
	DatabaseHandler database;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		//retrieve the article text from the database
		//TODO check the data exists in the database
		int id = this.getIntent().getIntExtra("id", 0);
		database = new DatabaseHandler(this);
		String html = database.getHtml(id);
		//display the article
		WebView webView = (WebView)findViewById(R.id.articleWebView);
		webView.loadData(html, "text/html", "utf-8");
	}
}
