package com.digitallizard.bbcnewsreader.fragments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.SupportActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.digitallizard.bbcnewsreader.ItemAdapter;
import com.digitallizard.bbcnewsreader.NewsItem;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ResourceService;
import com.digitallizard.bbcnewsreader.ServiceManager;
import com.digitallizard.bbcnewsreader.ServiceManager.MessageReceiver;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.fragments.FrontpageFragment.FrontPageClickHandler;

public class CategoryFragment extends Fragment implements MessageReceiver {
	//public static final int THUMBNAIL_WIDTH_PX = 144;
	//public static final int THUMBNAIL_HEIGHT_PX = 81;
	public static final int THUMBNAIL_WIDTH_PX = 216;
	public static final int THUMBNAIL_HEIGHT_PX = 121;
	
	DatabaseHandler database;
	ServiceManager service;
	FrontPageClickHandler clickHandler;
	
	GridView grid;
	ArrayList<NewsItem> items;
	
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
				NewsItem item = (NewsItem) grid.getAdapter().getItem(position);
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
						NewsItem item = (NewsItem) list.getAdapter().getItem(i);
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
		
		return view;
	}
	
	@Override
	public void onAttach(SupportActivity activity) {
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
		int rowPixelWidth = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		//int rowPixelWidth = this.getView().getLayoutParams().width;
		int rowLength = (int) Math.floor(rowPixelWidth / THUMBNAIL_WIDTH_PX);
		int thumbWidth =  (int) Math.floor(rowPixelWidth / rowLength);
		int thumbHeight = (int) Math.floor((thumbWidth / THUMBNAIL_WIDTH_PX) * THUMBNAIL_HEIGHT_PX);
		grid.setNumColumns(rowLength);
		
		// find the items and add them to the list
		items = new ArrayList<NewsItem>(Arrays.asList(database.getItems(categoryTitle, 28))); // specify a high limit for the number of items
		grid.setAdapter(new ItemAdapter(getActivity(), R.layout.list_news_item, items, thumbWidth, thumbHeight));
	}
	
	private void thumbLoadComplete(int id) {
		// load the thumbnail
		byte[] thumbnailBytes = database.getThumbnail(id);
		// loop through and set this thumbnail
		Iterator<NewsItem> iterator = items.iterator();
		while (iterator.hasNext()) {
			NewsItem item = iterator.next();
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
