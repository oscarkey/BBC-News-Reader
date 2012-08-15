/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.digitallizard.bbcnewsreader.fragments.ArticleFragment;

public class ArticleActivity extends SherlockFragmentActivity {
	public static final String EXTRA_KEY_ITEM_ID = "itemId";
	
	private int itemId;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_activity);
		
		// get the article id
		itemId = getIntent().getIntExtra(EXTRA_KEY_ITEM_ID, -6);
		
		// load the requested article in the fragment
		ArticleFragment fragment = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.articleFragment);
		fragment.displayArticle(itemId);
		
		// hide the action bar
		getSupportActionBar().hide();
		
		// create the ad
		/*
		 * AdView adView = new AdView(this, AdSize.BANNER, ReaderActivity.AD_PUB_ID); AdRequest adRequest = new AdRequest();
		 * adRequest.addTestDevice(AdRequest.TEST_EMULATOR); adRequest.addTestDevice("E1E103563E9BF5BD900001A4831258C1"); adView.loadAd(adRequest);
		 * LinearLayout layout = (LinearLayout) findViewById(R.id.articleActivityContainer); layout.addView(adView);
		 */
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			// go back
			this.finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
}