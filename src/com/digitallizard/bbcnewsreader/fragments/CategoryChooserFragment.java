package com.digitallizard.bbcnewsreader.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.digitallizard.bbcnewsreader.CategoryChooserAdapter;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.mobeta.android.dslv.DragSortListView;

public class CategoryChooserFragment extends SherlockFragment {
	
	/* constants */

	/* variables */
	private DatabaseHandler database;
	private CategoryChooserAdapter adapter;
	private DragSortListView listView;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// load the categories from the database
		database = new DatabaseHandler(getActivity());
		String[] enabledCategories = database.getEnabledCategories()[1];
		String[] disabledCategories = database.getDisabledCategories()[1];
		
		// create the list adapter
		adapter = new CategoryChooserAdapter(getActivity(), 
				enabledCategories, disabledCategories);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the layout
		View view = inflater.inflate(R.layout.category_chooser, container, false);
		
		// connect the listview to the adapter and set dragging stuff
		listView = (DragSortListView) view.findViewById(R.id.categoryChooserListView);
		listView.setAdapter(adapter);
		listView.setDropListener(dropListener);
				
		return view;
	}
	
	private DragSortListView.DropListener dropListener = new DragSortListView.DropListener() {
		@Override
		public void drop(int from, int to) {
			// remove the item from the old position and insert it at the new one
			if(from != to) {
				String item = adapter.getItem(from);
				adapter.move(item, from, to);
			}
		}
	};
	
	public void saveCategories() {
		database.setCategoryStates(adapter.getEnabledCategories(), adapter.getDisabledCategories());
	}
}
