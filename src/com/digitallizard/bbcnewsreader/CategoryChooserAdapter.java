package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class CategoryChooserAdapter extends BaseAdapter implements ListAdapter {
	/* constants */
	//FIXME should be in xml
	private static final String ENABLED_HEADER_TEXT = "Enabled categories";
	private static final String DISABLED_HEADER_TEXT = "Disabled categories";
	private static final int VIEW_TYPE_COUNT = 2;
	private static final int VIEW_TYPE_HEADER = 0;
	private static final int VIEW_TYPE_CATEGORY = 1;
	
	/* variables */
	private LayoutInflater inflater;
	private ArrayList<String> items;
	private int enabledHeaderPosition, disabledHeaderPosition;
	
	public CategoryChooserAdapter(Context context, 
			String[] enabledCategories, String[] disabledCategories) {		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// build the items list
		this.items = new ArrayList<String>();
		items.add(ENABLED_HEADER_TEXT);
		items.addAll(Arrays.asList(enabledCategories));
		items.add(DISABLED_HEADER_TEXT);
		items.addAll(Arrays.asList(disabledCategories));
		updateHeaderPositions();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// determine if we should be returning a header or an actual item
		if(getItemViewType(position) == VIEW_TYPE_HEADER) {
			// init a new view if convert view is not of the correct type or is null
			if(convertView == null || convertView.getId() != R.id.categoryChooserHeaderItem) {
				convertView = inflater.inflate(R.layout.category_chooser_header_item, null);
			}
			
			TextView header = (TextView) convertView;
			if(position == enabledHeaderPosition) {
				// set the enabled header
				header.setText(ENABLED_HEADER_TEXT);
			}
			else if(position == disabledHeaderPosition) {
				// set the disabled header
				header.setText(DISABLED_HEADER_TEXT);
			}
			return header;
		}
		else {
			// return an actual item
			// init a new view if convert view is not of the correct type or is null
			if(convertView == null || convertView.getId() != R.id.categoryChooserSortableItem) {
				convertView = inflater.inflate(R.layout.category_chooser_sortable_item, null);
			}
			
			// set the item text and return it
			((TextView) convertView.findViewById(R.id.categoryChooserItemText)).setText(getItem(position));
			return convertView;
		}
	}
	
	public void move(String item, int from, int to) {
		// check that the item is not being moved above the enabled header
		if(to <= enabledHeaderPosition) {
			to = enabledHeaderPosition + 1;
		}
		
		// remove the item and then insert it again
		items.remove(item);
		items.add(to, item);
		
		// notify the about the change
		updateHeaderPositions();
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public String getItem(int position) {
		// return the item at this position
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getPosition(String item) {
		return items.indexOf(item);
	}

	@Override
	public int getItemViewType(int position) {
		// if the item is at 0 or the end of the first list it is a header
		if(position == enabledHeaderPosition || position == disabledHeaderPosition) {
			return VIEW_TYPE_HEADER;
		} else {
			// it's a category
			return VIEW_TYPE_CATEGORY;
		}
	}
	
	@Override
	public int getViewTypeCount() {
		// we have two types of view
		return VIEW_TYPE_COUNT;
	}

	public String[] getEnabledCategories() {
		// build an array of enabled categories
		ArrayList<String> categories = new ArrayList<String>();
		for(int i = (enabledHeaderPosition + 1); i < disabledHeaderPosition; i++) {
			categories.add(items.get(i));
		}
		return categories.toArray(new String[categories.size()]);
	}
	
	public String[] getDisabledCategories() {
		// build array of disabled categories
		ArrayList<String> categories = new ArrayList<String>();
		for(int i = (disabledHeaderPosition + 1); i < items.size(); i++) {
			categories.add(items.get(i));
		}
		return categories.toArray(new String[categories.size()]);
	}
	
	private void updateHeaderPositions() {
		enabledHeaderPosition = 0;
		disabledHeaderPosition = items.indexOf(DISABLED_HEADER_TEXT);
	}
}
