/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.support.v4.view.ViewPager;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.fragments.FrontpageFragment.FrontPageClickHandler;
import com.viewpagerindicator.TitlePageIndicator;

public class CategoryActivity extends SherlockFragmentActivity implements FrontPageClickHandler {
	public static final String EXTRA_CATEGORY_TITLE = "categoryTitle";
	
	private static final int DISPLAY_MODE_HANDSET = 0;
	private static final int DISPLAY_MODE_TABLET_LANDSCAPE = 1;
	
	ListView listView;
	DatabaseHandler database;
	ArrayList<Item> items;
	Messenger resourceMessenger;
	boolean resourceServiceBound;
	
	private int currentDisplayMode;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.category_activity);
		
		// determine which display mode is currently active
		if (getResources().getBoolean(R.bool.screen_xlarge)) {
			currentDisplayMode = DISPLAY_MODE_TABLET_LANDSCAPE;
		}
		else {
			currentDisplayMode = DISPLAY_MODE_HANDSET;
		}
		
		// do specific configuration for various screen sizes
		/*if (currentDisplayMode == DISPLAY_MODE_TABLET_LANDSCAPE) {
			// force landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			// display the correct category
			CategoryFragment fragment = (CategoryFragment) getSupportFragmentManager().findFragmentById(R.id.categoryFragment);
			fragment.displayCategory(getIntent().getStringExtra(EXTRA_CATEGORY_TITLE));
		}*/
		
		if (currentDisplayMode == DISPLAY_MODE_HANDSET) {
			// load the pager
			CategoryPagerAdapter adapter = new CategoryPagerAdapter(this, getSupportFragmentManager());
	        ViewPager pager = (ViewPager)findViewById(R.id.categoryPager);
	        pager.setAdapter(adapter);
	        
	        // bind the pager indicator
	        TitlePageIndicator pagerIndicator = (TitlePageIndicator) findViewById(R.id.categoryPagerIndicator);
	        pagerIndicator.setViewPager(pager);
	        
	        // set the pager position to the selected category
	        pager.setCurrentItem(adapter.getPositionOfTitle(getIntent().getStringExtra(EXTRA_CATEGORY_TITLE)), false); // no smooth scroll
		}
	}
	
	public void onItemClick(int id) {
		// launch the article activity
		Intent intent = new Intent(this, ArticleActivity.class);
		intent.putExtra(ArticleActivity.EXTRA_KEY_ITEM_ID, id);
		startActivity(intent);
	}
	
	public void onCategoryClick(String title) {
		// this should never be called in this case
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			this.finish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
}
