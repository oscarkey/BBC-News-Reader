package com.digitallizard.bbcnewsreader.fragments;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.digitallizard.bbcnewsreader.Item;
import com.digitallizard.bbcnewsreader.ItemLayout;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.ResourceService;
import com.digitallizard.bbcnewsreader.ServiceManager;
import com.digitallizard.bbcnewsreader.ServiceManager.MessageReceiver;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class FrontpageFragment extends SherlockFragment implements MessageReceiver {
	private static final int ITEM_MIN_WIDTH = 100;
	private static final int MAX_ITEMS_PER_ROW_PORTRAIT = 3;
	private static final int MAX_ITEMS_PER_ROW_LANDSCAPE = 5;
	private static final int MAX_ITEMS_PER_ROW_TABLET_LANDSCAPE = 5;
	private static final int PORTRAIT_CODE = 0;
	private static final float THUMB_HEIGHT_RATIO = 9/16f;
	private static final float FRAGMENT_SCREEN_RATIO = 3/5f; //FIXME this is not the right way to do this
	
	private DatabaseHandler database;
	private ServiceManager service;
	
	private FrontPageClickHandler clickHandler;
	
	private String[] categoryNames;
	private ArrayList<RelativeLayout> physicalCategories;
	private ItemLayout[][] physicalItems;
	private int categoryRowLength; // the number of items to show per row
	
	public interface FrontPageClickHandler {
		public void onItemClick(int id);
		
		public void onCategoryClick(String title);
	}
	
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case ResourceService.MSG_FULL_LOAD_COMPLETE:
			loadUnloadedThumbs();
			break;
		case ResourceService.MSG_CATEGORY_LOADED:
			categoryLoadFinished(msg.getData().getString(ResourceService.KEY_CATEGORY));
			break;
		case ResourceService.MSG_THUMB_LOADED:
			updateItemThumb(msg.getData().getInt(ResourceService.KEY_ITEM_ID));
			break;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// bind to the service
		service = new ServiceManager(getActivity(), this);
		service.doBindService();
		
		// bind to the database
		database = new DatabaseHandler(this.getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frontpage, container, false);
		createNewsDisplay(inflater, view);
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
		// unbind the service
		service.doUnbindService();
	}
	
	public void createNewsDisplay(LayoutInflater inflater, View container) {
		//FIXME entire layout system needs fixing
		LinearLayout content = (LinearLayout) container.findViewById(R.id.newsScrollerContent); // a reference to the layout where we put the news
		// clear the content area
		content.removeAllViewsInLayout();
		
		// find the width and work out how many items we can add and how wide they should be
		int rowPixelWidth = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		// take into account screen orientation and device type
		int maxItemsPerRow = MAX_ITEMS_PER_ROW_PORTRAIT;
		if(((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation() != PORTRAIT_CODE) {
			maxItemsPerRow = MAX_ITEMS_PER_ROW_LANDSCAPE;
		}
		//FIXME dangerous
		if(((ReaderActivity) getActivity()).getCurrentDisplayMode() == ReaderActivity.DISPLAY_MODE_TABLET_LANDSCAPE) {
			rowPixelWidth = (int) Math.floor(rowPixelWidth * FRAGMENT_SCREEN_RATIO);
			maxItemsPerRow = MAX_ITEMS_PER_ROW_TABLET_LANDSCAPE;
		}
		int rowWidth = (int) Math.floor(rowPixelWidth / this.getResources().getDisplayMetrics().density); // formula to convert from pixels to dp
		categoryRowLength = (int) Math.floor(rowWidth / ITEM_MIN_WIDTH);
		if (categoryRowLength > maxItemsPerRow) {
			categoryRowLength = maxItemsPerRow;
		}
		int thumbWidth = (int) Math.floor(rowPixelWidth / categoryRowLength);
		int thumbHeight = (int) Math.floor(((float) thumbWidth) * THUMB_HEIGHT_RATIO);
		
		// create the categories
		categoryNames = database.getEnabledCategories()[1]; // string array with category names in it
		physicalCategories = new ArrayList<RelativeLayout>(categoryNames.length);
		physicalItems = new ItemLayout[categoryNames.length][categoryRowLength]; // the array to hold the news items
		
		// loop through adding category views
		for (int i = 0; i < categoryNames.length; i++) {
			// create the category title bar
			RelativeLayout categoryTitleBar = (RelativeLayout) inflater.inflate(R.layout.list_category_item, null);
			
			// set a click listener
			categoryTitleBar.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// FIXME there must be a more elegant way of doing this...
					// find the id of the category and report the click
					int id = physicalCategories.indexOf(view);
					clickHandler.onCategoryClick(categoryNames[id]);
				}
			});
			
			// change the name
			TextView name = (TextView) categoryTitleBar.findViewById(R.id.categoryTitle);
			name.setText(categoryNames[i]);
			
			// store the category title bar and add it to the scroller
			physicalCategories.add(i, categoryTitleBar); // store the category for future use
			content.addView(categoryTitleBar);
			
			// create an item row and add items to it
			//TableRow itemRow = new TableRow(getActivity());
			LinearLayout itemRow = new LinearLayout(getActivity());
			
			for (int j = 0; j < categoryRowLength; j++) {
				// add a new item to the display
				ItemLayout item = (ItemLayout) inflater.inflate(R.layout.list_news_item, null);
				item.setPadding(1, 0, 1, 0);
				item.setImageSize(thumbWidth, thumbHeight);
				
				// set a click listener
				item.setOnClickListener(new View.OnClickListener() {
					public void onClick(View view) {
						// retrieve the title of this activity
						ItemLayout item = (ItemLayout) view; // cast the view to a an itemlayout
						// check there is an item at this view
						if (item.isItem()) {
							// report the click
							clickHandler.onItemClick(item.getId());
						}
					}
				});
				
				physicalItems[i][j] = item; // store the item for future use
				itemRow.addView(item); // add the item to the display
			}
			
			// add the row to the display
			content.addView(itemRow);
			
			// populate this category with news
			displayCategoryItems(i);
		}
	}
	
	private void displayCategoryItems(int category) {
		// load from the database, if there's anything in it
		Item[] items = database.getItems(categoryNames[category], categoryRowLength);
		if (items != null) {
			// change the physical items to match this
			for (int i = 0; i < categoryRowLength; i++) {
				// check we have not gone out of range of the available news
				if (i < items.length) {
					physicalItems[category][i].setTitle(items[i].getTitle());
					physicalItems[category][i].setId(items[i].getId());
					
					// try and get an thumbnail for this item
					byte[] thumbBytes = items[i].getThumbnailBytes();
					// check if any image data was returned
					if (Arrays.equals(thumbBytes, ReaderActivity.NO_THUMBNAIL_URL_CODE)) {
						// set the image to the no thumbnail image
						physicalItems[category][i].setImage(R.drawable.no_thumb);
						physicalItems[category][i].setImageLoaded(false);
					}
					else if (thumbBytes != null) {
						// try to construct an image out of the bytes given by the database
						Bitmap imageBitmap = BitmapFactory.decodeByteArray(thumbBytes, 0, thumbBytes.length); // load the image into a bitmap
						physicalItems[category][i].setImage(imageBitmap);
						physicalItems[category][i].setImageLoaded(true);
					}
					else {
						// set the image to the default grey image
						physicalItems[category][i].setImage(R.drawable.no_thumb_grey);
						physicalItems[category][i].setImageLoaded(false);
					}
				}
			}
		}
	}
	
	private void categoryLoadFinished(String category) {
		// the database has finished loading a category, we can update
		// FIXME very inefficient way to turn (string) name into (int) id
		int id = 0; // the id of the category
		for (int i = 0; i < categoryNames.length; i++) {
			// check if the name we have been given matches this category
			if (category.equals(categoryNames[i])) {
				id = i;
			}
		}
		displayCategoryItems(id); // redisplay this category
	}
	
	private void updateItemThumb(int id) {
		// loop through categories
		for (int i = 0; i < physicalItems.length; i++) {
			for (int t = 0; t < physicalItems[i].length; t++) {
				if (physicalItems[i][t].getId() == id) {
					// try and get an image for this item
					byte[] imageBytes = database.getThumbnail(id);
					// check if any image data was returned
					if (Arrays.equals(imageBytes, ReaderActivity.NO_THUMBNAIL_URL_CODE)) {
						// sets the image to the no thumbnail image
						physicalItems[i][t].setImage(R.drawable.no_thumb);
						physicalItems[i][t].setImageLoaded(false);
					}
					else if (imageBytes != null) {
						// try to construct an image out of the bytes given by the database
						Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); // load the image into a bitmap
						physicalItems[i][t].setImage(imageBitmap);
						physicalItems[i][t].setImageLoaded(true);
					}
					else {
						// set the image to the no thumbnail loaded image
						physicalItems[i][t].setImage(R.drawable.no_thumb_grey);
						physicalItems[i][t].setImageLoaded(false);
					}
				}
			}
		}
	}
	
	private void loadUnloadedThumbs() {
		// find unloaded images
		for(int i = 0; i < physicalItems.length; i++) {
			for(int j = 0; j < physicalItems[i].length; j++) {
				// only try and load the images if the item is set
				if(!physicalItems[i][j].isImageLoaded() && physicalItems[i][j].isItem()) {
					Bundle bundle = new Bundle();
					bundle.putInt(ResourceService.KEY_ITEM_ID, physicalItems[i][j].getId());
					service.sendMessageToService(ResourceService.MSG_LOAD_THUMB, bundle);
				}
			}
		}
	}
}
