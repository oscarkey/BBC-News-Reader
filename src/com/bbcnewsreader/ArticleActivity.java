/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.bbcnewsreader;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class ArticleActivity extends Activity {
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		WebView webView = (WebView)findViewById(R.id.articleWebView);
		webView.getUrl();
		webView.loadUrl(getIntent().getStringExtra("url"));
	}
}
