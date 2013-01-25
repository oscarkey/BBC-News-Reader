package com.digitallizard.bbcnewsreader;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.fragments.CategoryFragment;

public class CategoryPagerAdapter extends FragmentStatePagerAdapter {
	
	private String[] enabledCategoryTitles;
	
	public CategoryPagerAdapter(Context context, FragmentManager fragmentManager) {
		super(fragmentManager);
		
		// get a list of enabled categories
		//FIXME should be done outside of the adapter
		DatabaseHandler database = new DatabaseHandler(context);
		enabledCategoryTitles = database.getEnabledCategories()[1];
	}
	
	@Override
	public Fragment getItem(int position) {
		return CategoryFragment.newInstance(enabledCategoryTitles[position]);
	}

	@Override
	public int getCount() {
		return enabledCategoryTitles.length;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		return enabledCategoryTitles[position];
	}
	
	public int getPositionOfTitle(String title) {
		for(int i = 0; i < enabledCategoryTitles.length; i++) {
			if(enabledCategoryTitles[i].equals(title)) {
				return i;
			}
		}
		
		return -98; // should never happen
	}

}
