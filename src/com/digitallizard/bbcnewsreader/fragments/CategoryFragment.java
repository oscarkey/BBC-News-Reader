package com.digitallizard.bbcnewsreader.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.actionbarsherlock.app.SherlockFragment;
import com.digitallizard.bbcnewsreader.Item;
import com.digitallizard.bbcnewsreader.ItemAdapter;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ResourceService;
import com.digitallizard.bbcnewsreader.ServiceManager;
import com.digitallizard.bbcnewsreader.ServiceManager.MessageReceiver;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.fragments.FrontpageFragment.FrontPageClickHandler;

public class CategoryFragment extends SherlockFragment implements MessageReceiver {
	//public static final int THUMBNAIL_WIDTH_PX = 144;
	//public static final int THUMBNAIL_HEIGHT_PX = 81;
	//public static final int THUMBNAIL_WIDTH_PX = 216;
	//public static final int THUMBNAIL_HEIGHT_PX = 121;
	public static final int THUMBNAIL_WIDTH_PX = 259;
	public static final int THUMBNAIL_HEIGHT_PX = 145;
	public static final int MIN_ROW_LENGTH = 2;
	public static final String KEY_CATEGORY = "category";
	
	DatabaseHandler database;
	ServiceManager service;
	FrontPageClickHandler clickHandler;
	
	GridView grid;
	ArrayList<Item> items;
	
	
	public static CategoryFragment newInstance(String category) {
		CategoryFragment fragment = new CategoryFragment();
		
		Bundle args = new Bundle();
		args.putString(KEY_CATEGORY, category);
		fragment.setArguments(args);
		
		return fragment;
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		database = new DatabaseHandler(getActivity());
		service = new ServiceManager(getActivity(), this);
		
		service.doBindService();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.category, container, false);
		
		// set up the grid
		grid = (GridView) view.findViewById(R.id.categoryNewsItemGrid);
		
		// add a listener to detect clicks
		grid.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// retrieve the NewsItem at the index of the click
				Item item = (Item) grid.getAdapter().getItem(position);
				// view the item
				clickHandler.onItemClick(item.getId());
			}
		});
		
		// add a listener to detect scrolls
		grid.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView list, int state) {
				// check to see if the user has stopped scrolling
				if (state == OnScrollListener.SCROLL_STATE_IDLE) {
					// check to see if all the visible items have images
					int firstVisible = list.getFirstVisiblePosition();
					int lastVisible = list.getLastVisiblePosition();
					for (int i = firstVisible; i <= lastVisible; i++) {
						Item item = (Item) list.getAdapter().getItem(i);
						// if this item doesn't have a thumbnail
						if (item.getThumbnailBytes() == null) {
							// load the thumbnail
							Bundle bundle = new Bundle();
							bundle.putInt(ResourceService.KEY_ITEM_ID, item.getId());
							service.sendMessageToService(ResourceService.MSG_LOAD_THUMB, bundle);
						}
					}
				}
			}
			
			public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
			}
		});
		
		// check if we have been provided with a category to display
		if(getArguments().getString(KEY_CATEGORY) != null) {
			displayCategory(getArguments().getString(KEY_CATEGORY));
		}
		
		return view;
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// check that the parent has implemented the correct callbacks
		try {
			clickHandler = (FrontPageClickHandler) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement FrontPageClickHandlers");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		service.doUnbindService();
		((ItemAdapter) grid.getAdapter()).finish();
	}
	
	public void displayCategory(String categoryTitle) {
		// work out the ideal thumbnail size
		int rowPixelWidth = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE))
				.getDefaultDisplay().getWidth();
		//int rowPixelWidth = this.getView().getLayoutParams().width;
		
		// check the row isn't too small
		int rowLength = (int) Math.floor(rowPixelWidth / THUMBNAIL_WIDTH_PX);
		if(rowLength < MIN_ROW_LENGTH) {
			rowLength = MIN_ROW_LENGTH;
		}
		
		int thumbWidth =  (int) Math.floor(rowPixelWidth / rowLength);
		int thumbHeight = (int) Math.floor(((float) thumbWidth / (float) THUMBNAIL_WIDTH_PX) * (float) THUMBNAIL_HEIGHT_PX);
		grid.setNumColumns(rowLength);
		grid.setColumnWidth(thumbWidth);
		
		// find the items and add them to the list
		items = new ArrayList<Item>(Arrays.asList(database.getItems(categoryTitle, 28))); // specify a high limit for the number of items
		grid.setAdapter(new ItemAdapter(getActivity(), R.layout.list_news_item, items, thumbWidth, thumbHeight));
	}
	
	private void thumbLoadComplete(int id) {
		// load the thumbnail
		byte[] thumbnailBytes = database.getThumbnail(id);
		// loop through and set this thumbnail
		Iterator<Item> iterator = items.iterator();
		while (iterator.hasNext()) {
			Item item = iterator.next();
			if (item.getId() == id) {
				// set the thumbnail bytes
				item.setThumbnailBytes(thumbnailBytes);
			}
		}
		
		// refresh this view in the list
		((ItemAdapter) grid.getAdapter()).notifyDataSetChanged();
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case ResourceService.MSG_THUMB_LOADED:
			// display the thumbnail
			int id = msg.getData().getInt(ResourceService.KEY_ITEM_ID);
			thumbLoadComplete(id);
			break;
		}
	}
}
