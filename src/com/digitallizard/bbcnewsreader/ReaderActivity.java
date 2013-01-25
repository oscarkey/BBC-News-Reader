/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.digitallizard.bbcnewsreader.ServiceManager.MessageReceiver;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.fragments.ArticleFragment;
import com.digitallizard.bbcnewsreader.fragments.FrontpageFragment;
import com.digitallizard.bbcnewsreader.fragments.FrontpageFragment.FrontPageClickHandler;

public class ReaderActivity extends SherlockFragmentActivity implements MessageReceiver, FrontPageClickHandler {
	
	/* constants */
	public static final String AD_PUB_ID = "a14f0749c716805";
	
	static final int ACTIVITY_CHOOSE_CATEGORIES = 1;
	
	public static final int DISPLAY_MODE_HANDSET = 0;
	public static final int DISPLAY_MODE_TABLET_LANDSCAPE = 1;
	
	public static final String PREFS_FILE_NAME = "com.digitallizard.bbcnewsreader_preferences";
	public static final int DEFAULT_ITEM_LOAD_LIMIT = 4;
	public static final int DEFAULT_CLEAR_OUT_AGE = 4;
	public static final boolean DEFAULT_LOAD_IN_BACKGROUND = true;
	public static final boolean DEFAULT_RTC_WAKEUP = true;
	public static final String DEFAULT_LOAD_INTERVAL = "1_hour";
	public static final boolean DEFAULT_DISPLAY_FULL_ERROR = false;
	public static final int DEFAULT_CATEGORY_UPDATE_VERSION = 0;
	public static final int CURRENT_CATEGORY_UPDATE_VERSION = 0;
	
	public static final String PREFKEY_LOAD_IN_BACKGROUND = "loadInBackground";
	public static final String PREFKEY_RTC_WAKEUP = "rtcWakeup";
	public static final String PREFKEY_LOAD_INTERVAL = "loadInterval";
	public static final String PREFKEY_CATEGORY_UPDATE_VERSION = "categoryUpdateVersion";
	
	public static final int ERROR_TYPE_GENERAL = 0;
	public static final int ERROR_TYPE_INTERNET = 1;
	public static final int ERROR_TYPE_FATAL = 2;
	
	public static final byte[] NO_THUMBNAIL_URL_CODE = new byte[] { 127 };
	
	/* variables */
	private DatabaseHandler database;
	private ServiceManager service;
	private SharedPreferences settings;
	
	private int currentDisplayMode;
	
	private boolean loadInProgress;
	private long lastLoadTime;
	private boolean errorWasFatal;
	private boolean errorDuringThisLoad;
	private boolean firstRun;
	
	private Dialog errorDialog;
	private Dialog firstRunDialog;
	private Dialog backgroundLoadDialog;
	
	private Menu menu;
	
	public void handleMessage(Message msg) {
		// decide what to do with the message
		switch (msg.what) {
		case ResourceService.MSG_CLIENT_REGISTERED:
			// start a load if we haven't loaded within half an hour
			// TODO make the load time configurable
			long difference = System.currentTimeMillis() - (lastLoadTime * 1000); // the time since the last load
			if (lastLoadTime == 0 || difference > (60 * 60 * 1000)) {
				// don't load if this is the first run
				if(!firstRun) {
					loadData(); // trigger a load
				}
			}
			break;
		case ResourceService.MSG_ERROR:
			Bundle bundle = msg.getData(); // retrieve the data
			errorOccured(bundle.getInt(ResourceService.KEY_ERROR_TYPE), bundle.getString(ResourceService.KEY_ERROR_MESSAGE),
					bundle.getString(ResourceService.KEY_ERROR_ERROR));
			break;
		case ResourceService.MSG_NOW_LOADING:
			loadBegun();
			break;
		case ResourceService.MSG_FULL_LOAD_COMPLETE:
			fullLoadComplete();
			break;
		case ResourceService.MSG_RSS_LOAD_COMPLETE:
			rssLoadComplete();
			break;
		case ResourceService.MSG_UPDATE_LOAD_PROGRESS:
			int totalItems = msg.getData().getInt("totalItems");
			int itemsLoaded = msg.getData().getInt("itemsDownloaded");
			updateLoadProgress(totalItems, itemsLoaded);
			break;
		}
	}
	
	public void onItemClick(int id) {
		// either transition to the article view activity or update the article fragment
		if (currentDisplayMode == DISPLAY_MODE_HANDSET) {
			// launch the article activity
			Intent intent = new Intent(this, ArticleActivity.class);
			intent.putExtra(ArticleActivity.EXTRA_KEY_ITEM_ID, id);
			startActivity(intent);
		}
		else if (currentDisplayMode == DISPLAY_MODE_TABLET_LANDSCAPE) {
			// display the article
			ArticleFragment article = (ArticleFragment) getSupportFragmentManager().findFragmentById(R.id.articleFragment);
			article.displayArticle(id);
			// findViewById(R.id.articleFragment)
		}
	}
	
	public void onCategoryClick(String title) {
		// either transition to the article view activity or update the article fragment
		if (currentDisplayMode == DISPLAY_MODE_HANDSET) {
			// launch the category activity
			Intent intent = new Intent(this, CategoryActivity.class);
			intent.putExtra(CategoryActivity.EXTRA_CATEGORY_TITLE, title);
			startActivity(intent);
		}
	}
	
	void errorOccured(int type, String msg, String error) {
		// check if we need to fill in the error messages
		if (msg == null) {
			msg = "null";
		}
		if (error == null) {
			error = "null";
		}
		
		// check if we need to shutdown after displaying the message
		if (type == ERROR_TYPE_FATAL) {
			errorWasFatal = true;
		}
		
		// show a user friendly message or just the error
		if (settings.getBoolean("displayFullError", DEFAULT_DISPLAY_FULL_ERROR)) {
			showErrorDialog("Error: " + error);
		}
		else {
			// display a user friendly message
			if (type == ERROR_TYPE_FATAL) {
				showErrorDialog("Fatal error:\n" + msg + "\nPlease try resetting the app.");
				Log.e("BBC News Reader", "Fatal error: " + msg);
				Log.e("BBC News Reader", error);
			}
			else if (type == ERROR_TYPE_GENERAL) {
				showErrorDialog("Error:\n" + msg);
				Log.e("BBC News Reader", "Error: " + msg);
				Log.e("BBC News Reader", error);
			}
			else if (type == ERROR_TYPE_INTERNET) {
				// only allow one internet error per load
				if (!errorDuringThisLoad) {
					errorDuringThisLoad = true;
					showErrorDialog("Please check your internet connection.");
				}
				Log.e("BBC News Reader", "Error: " + msg);
				Log.e("BBC News Reader", error);
			}
		}
	}
	
	void showErrorDialog(String error) {
		// only show the error dialog if one isn't already visible
		if (errorDialog == null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(error);
			builder.setCancelable(false);
			builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					closeErrorDialog();
				}
			});
			errorDialog = builder.create();
			errorDialog.show();
		}
	}
	
	void closeErrorDialog() {
		errorDialog = null; // destroy the dialog
		// see if we need to end the program
		if (errorWasFatal) {
			// crash out
			// Log.e("BBC News Reader", "Oops something broke. We'll crash now.");
			System.exit(1); // closes the app with an error code
		}
	}
	
	void showFirstRunDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = "Choose the categories you are interested in. \n\n"
				+ "The fewer categories enabled the lower data usage and the faster loading will be.";
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton("Choose", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				closeFirstRunDialog();
				// show the category chooser
				showCategoryChooser();
			}
		});
		firstRunDialog = builder.create();
		firstRunDialog.show();
	}
	
	void closeFirstRunDialog() {
		firstRunDialog = null; // destroy the dialog
	}
	
	void showBackgroundLoadDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String message = "Load news in the background? \n\n" + "This could increase data usage but will reduce load times.\n\n"
				+ "If you wish to use the widget, this should be switched on.";
		builder.setMessage(message);
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				closeBackgroundLoadDialog();
				firstRun = false; // we have finished the first run
				// save the selected option
				Editor editor = settings.edit();
				editor.putBoolean("loadInBackground", true);
				editor.commit();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				closeBackgroundLoadDialog();
				firstRun = false; // we have finished the first run
				// save the selected option
				Editor editor = settings.edit();
				editor.putBoolean("loadInBackground", false);
				editor.commit();
			}
		});
		backgroundLoadDialog = builder.create();
		backgroundLoadDialog.show();
	}
	
	void closeBackgroundLoadDialog() {
		backgroundLoadDialog = null;
	}
	
	void updateLoadProgress(int totalItems, int itemsLoaded) {
		setStatusText("Preloading " + itemsLoaded + " of " + totalItems + " items");
	}
	
	void setLastLoadTime(long time) {
		lastLoadTime = time; // store the time
		// display the new time to the user
		// check if the time is set
		if (!loadInProgress) {
			if (lastLoadTime == 0) {
				// say we have never loaded
				setStatusText("Never updated.");
			}
			else {
				// set the text to show date and time
				String status = "Updated ";
				// find out time since last load in milliseconds
				long difference = System.currentTimeMillis() - (time * 1000); // the time since the last load
				// if within 1 hour, display minutes
				if (difference < (1000 * 60 * 60)) {
					int minutesAgo = (int) Math.floor((difference / 1000) / 60);
					if (minutesAgo == 0) {
						status += "just now";
					}
					else if (minutesAgo == 1) {
						status += minutesAgo + " min ago";
					}
					else {
						status += minutesAgo + " mins ago";
					}
				}
				else {
					// if we are within 24 hours, display hours
					if (difference < (1000 * 60 * 60 * 24)) {
						int hoursAgo = (int) Math.floor(((difference / 1000) / 60) / 60);
						if (hoursAgo == 1) {
							status += hoursAgo + " hour ago";
						}
						else {
							status += hoursAgo + " hours ago";
						}
					}
					else {
						// if we are within 2 days, display yesterday
						if (difference < (1000 * 60 * 60 * 48)) {
							status += "yesterday";
						}
						else {
							// we have not updated recently
							status += "ages ago";
							// TODO more formal message?
						}
					}
				}
				setStatusText(status);
			}
		}
	}
	
	void setStatusText(String text) {
		// set the text to the action bar
		getSupportActionBar().setTitle(text);
	}
	
	void loadBegun() {
		loadInProgress = true; // flag the data as being loaded
		// show the loading image on the button
		// TODO update the load button
		// tell the user what is going on
		setStatusText("Loading feeds...");
	}
	
	void loadData() {
		// check we aren't currently loading news
		if (!loadInProgress) {
			// TODO display old news as old
			// tell the service to load the data
			service.sendMessageToService(ResourceService.MSG_LOAD_DATA);
			errorDuringThisLoad = false;
			
			// hide the refresh button and show the stop button
			menu.findItem(R.id.menuItemRefresh).setVisible(false);
			menu.findItem(R.id.menuItemStop).setVisible(true);
		}
	}
	
	void stopDataLoad() {
		// check we are actually loading news
		if (loadInProgress) {
			errorDuringThisLoad = false;
			// send a message to the service to stop it loading the data
			service.sendMessageToService(ResourceService.MSG_STOP_DATA_LOAD);
			
			// hide the stop button and show the refresh button
			menu.findItem(R.id.menuItemRefresh).setVisible(true);
			menu.findItem(R.id.menuItemStop).setVisible(false);
		}
	}
	
	void fullLoadComplete() {
		// check we are actually loading news
		if (loadInProgress) {
			loadInProgress = false;
			
			// hide the stop button and show the refresh button
			menu.findItem(R.id.menuItemRefresh).setVisible(true);
			menu.findItem(R.id.menuItemStop).setVisible(false);
			
			// report the loaded status
			setLastLoadTime(settings.getLong("lastLoadTime", 0)); // set the time as unix time
			
			// tell the database to delete old items
			database.clearOld();
		}
	}
	
	void rssLoadComplete() {
		// check we are actually loading news
		if (loadInProgress) {
			// tell the user what is going on
			setStatusText("Loading items...");
		}
	}
	
	void showCategoryChooser() {
		// launch the category chooser activity
		Intent intent = new Intent(this, CategoryChooserActivity.class);
		startActivityForResult(intent, ACTIVITY_CHOOSE_CATEGORIES);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadInProgress = false;
		lastLoadTime = 0;
		
		// load the preferences system
		settings = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
		loadSettings();
		
		// load the database, init it if required
		database = new DatabaseHandler(this);
		firstRun = false;
		if (!database.isCreated()) {
			// add the categories and set the version to prevent this happening twice
			database.addCategoriesFromXml();
			Editor editor = settings.edit();
			editor.putInt(PREFKEY_CATEGORY_UPDATE_VERSION, CURRENT_CATEGORY_UPDATE_VERSION);
			editor.commit();
			
			// proceed with the first run
			firstRun = true;
			showFirstRunDialog();
		}
		
		// check if an update is required, if the stored category version is less than the current one
		if(settings.getInt(PREFKEY_CATEGORY_UPDATE_VERSION, DEFAULT_CATEGORY_UPDATE_VERSION) 
				< CURRENT_CATEGORY_UPDATE_VERSION) {
			// run an update
			database.updateCategoriesFromXml();
			
			// set the preference value to the current version
			Editor editor = settings.edit();
			editor.putInt(PREFKEY_CATEGORY_UPDATE_VERSION, CURRENT_CATEGORY_UPDATE_VERSION);
			editor.commit();
		}
		
		// bind the service
		service = new ServiceManager(this, this);
		service.doBindService();
		
		Eula.show(this); // show the eula
		
		// determine which display mode is currently active
		if (getResources().getBoolean(R.bool.screen_xlarge)) {
			currentDisplayMode = DISPLAY_MODE_TABLET_LANDSCAPE;
		}
		else {
			currentDisplayMode = DISPLAY_MODE_HANDSET;
		}
		
		// do specific configuration for various screen sizes
		if (currentDisplayMode == DISPLAY_MODE_TABLET_LANDSCAPE) {
			// force landscape
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}
		
		// hide the up button
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		
		// create the ui
		this.setContentView(R.layout.reader_activity);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// update the last loaded display
		setLastLoadTime(lastLoadTime);
		// TODO update display more often?
	}
	
	@Override
	protected void onDestroy() {
		// unbind from the service
		service.doUnbindService();
		super.onDestroy(); // pass the destroy command to the super
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// inflate the menu
		getSupportMenuInflater().inflate(R.menu.main_menu, menu);
		
		this.menu = menu; // store a reference for later
		
		return true; // we have made the menu so we can return true
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuItemCategories) {
			// launch the category chooser activity
			showCategoryChooser();
			return true;
		}
		else if (item.getItemId() == R.id.menuItemSettings) {
			// show the settings menu
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		}
		else if (item.getItemId() == R.id.menuItemRefresh) {
			loadData();
			return true;
		}
		else if (item.getItemId() == R.id.menuItemStop) {
			stopDataLoad();
			return true;
		}
		else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		// wait for activities to send us result data
		switch (requestCode) {
		case ACTIVITY_CHOOSE_CATEGORIES:
			// check the request was a success
			if (resultCode == RESULT_OK) {
				
				// refresh the display
				loadData(); // make sure selected categories are loaded
				FrontpageFragment frontpage = (FrontpageFragment) getSupportFragmentManager().findFragmentById(R.id.frontpageFragment);
				frontpage.createNewsDisplay(getLayoutInflater(), frontpage.getView()); // reload the ui
				
				// check for a first run
				if (firstRun) {
					showBackgroundLoadDialog();
				}
			}
			break;
		}
	}
	
	void loadSettings() {
		// check the settings file exists
		if (settings != null) {
			// load values from the settings
			setLastLoadTime(settings.getLong("lastLoadTime", 0)); // sets to zero if not in preferences
		}
	}
	
	public void refreshClicked(View item) {
		// start the load if we are not loading
		if (!loadInProgress) {
			loadData();
		}
		else {
			stopDataLoad();
		}
	}
	
	
	public int getCurrentDisplayMode() {
		return currentDisplayMode;
	}
}
