/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import android.app.Activity;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.digitallizard.bbcnewsreader.fragments.CategoryChooserFragment;

public class CategoryChooserActivity extends SherlockFragmentActivity {
	
	/* variables */
	CategoryChooserFragment fragment;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState); // load any saved state
		this.setContentView(R.layout.category_chooser_activity);
		fragment = (CategoryChooserFragment) 
				getSupportFragmentManager().findFragmentById(R.id.categoryChooserFragment);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.category_chooser_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// check which item was selected and react appropriately
		if (item.getItemId() == R.id.categoryChooserMenuItemSave) {
			saveCategoriesAndFinish();
			return true;
		}
		else if (item.getItemId() == android.R.id.home) {
			cancelAndFinish();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void saveCategoriesAndFinish() {
		fragment.saveCategories();
		setResult(Activity.RESULT_OK);
		finish();
	}
	
	private void cancelAndFinish() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
}
