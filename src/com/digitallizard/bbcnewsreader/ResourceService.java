/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;

import org.mcsoxford.rss.RSSItem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.resource.web.WebManager;
import com.digitallizard.bbcnewsreader.widget.ReaderWidget;

public class ResourceService extends Service implements ResourceInterface {
	/* variables */
	public boolean loadInProgress; // a flag to tell the activity if there is a load in progress
	ArrayList<Messenger> clients = new ArrayList<Messenger>(); // holds references to all of our clients
	final Messenger messenger = new Messenger(new IncomingHandler()); // the messenger used for communication
	BroadcastReceiver broadcastReceiver;
	DatabaseHandler database; // the database
	RSSManager rssManager;
	WebManager webManager;
	SharedPreferences settings;
	OnSharedPreferenceChangeListener settingsChangedListener;
	int totalItemsToDownload;
	int itemsDownloaded;
	
	/* command definitions */
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_CLIENT_REGISTERED = 3; // returned to a client when registered
	public static final int MSG_LOAD_DATA = 4; // sent to request a data load
	public static final int MSG_LOAD_ARTICLE = 11;
	public static final int MSG_LOAD_THUMB = 12;
	public static final int MSG_LOAD_IMAGE = 13;
	public static final int MSG_STOP_DATA_LOAD = 9; // sent to stop data loading
	public static final int MSG_CATEGORY_LOADED = 6; // sent when a category has loaded
	public static final int MSG_ARTICLE_LOADED = 15; // article loaded
	public static final int MSG_THUMB_LOADED = 14; // thumbnail loaded
	public static final int MSG_NOW_LOADING = 16;
	public static final int MSG_FULL_LOAD_COMPLETE = 8; // sent when all the data has been loaded
	public static final int MSG_RSS_LOAD_COMPLETE = 10;
	public static final int MSG_UPDATE_LOAD_PROGRESS = 18;
	public static final int MSG_ERROR = 7; // help! An error occurred
	public static final String KEY_ERROR_TYPE = "type";
	public static final String KEY_ERROR_MESSAGE = "message";
	public static final String KEY_ERROR_ERROR = "error";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_ITEM_ID = "itemId";
	public static final String ACTION_LOAD = "com.digitallizard.bbcnewsreader.action.LOAD_NEWS";
	
	// the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// decide what to do with the message
			switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo); // add a reference to the client to our list
				sendMsg(msg.replyTo, MSG_CLIENT_REGISTERED, null);
				break;
			case MSG_UNREGISTER_CLIENT:
				unregisterClient(msg.replyTo);
				break;
			case MSG_LOAD_DATA:
				loadData(); // start of the loading of data
				break;
			case MSG_LOAD_ARTICLE:
				loadArticle(msg.getData().getInt(KEY_ITEM_ID));
				break;
			case MSG_LOAD_THUMB:
				loadThumbnail(msg.getData().getInt("itemId"));
				break;
			case MSG_LOAD_IMAGE:
				// TODO load specific image
				break;
			case MSG_STOP_DATA_LOAD:
				stopDataLoad();
				break;
			default:
				super.handleMessage(msg); // we don't know what to do, lets hope that the super class knows
			}
		}
	}
	
	public class ResourceBinder extends Binder {
		ResourceService getService() {
			return ResourceService.this;
		}
	}
	
	public synchronized void setDatabase(DatabaseHandler db) {
		this.database = db;
	}
	
	public synchronized DatabaseHandler getDatabase() {
		return database;
	}
	
	public synchronized void setWebManager(WebManager manager) {
		this.webManager = manager;
	}
	
	public synchronized WebManager getWebManager() {
		return this.webManager;
	}
	
	void loadData() {
		// check if the device is online
		if (isOnline()) {
			// report to the gui that a load has been activated
			sendMsgToAll(MSG_NOW_LOADING, null);
			// set the flag saying that we are loading
			loadInProgress = true;
			// retrieve the active category urls
			String[][] enabledCategories = getDatabase().getEnabledCategories();
			String[] urls = enabledCategories[0];
			String[] names = enabledCategories[1];
			// start the RSS Manager
			rssManager.load(names, urls);
		}
		else {
			// report that there is no internet connection
			reportError(ReaderActivity.ERROR_TYPE_INTERNET, "There is no internet connection.", null);
		}
	}
	
	void loadArticle(int id) {
		String url = database.getUrl(id); // get the url of the item
		webManager.loadNow(url, WebManager.ITEM_TYPE_HTML, id); // tell the webmanager to load this
	}
	
	void loadThumbnail(int id) {
		String url = database.getThumbnailUrl(id); // get the url of the item
		if (url == null) {
			database.addThumbnail(id, ReaderActivity.NO_THUMBNAIL_URL_CODE);// Set thumbnail to no thumbnail
			// report that the thumbnail has been loaded so it can be displayed
			Bundle bundle = new Bundle();
			bundle.putInt("id", id);
			sendMsgToAll(MSG_THUMB_LOADED, bundle);
		}
		else {
			webManager.loadNow(url, WebManager.ITEM_TYPE_THUMB, id); // tell the webmanager to load this
		}
	}
	
	void loadImage(int id) {
		// TODO add specific image loading
	}
	
	void stopDataLoad() {
		// stop the data loading
		rssManager.stopLoading();
		getWebManager().stopDownload();
		// the stopping of loading will be reported by the managers...
	}
	
	void updateLastLoadTime() {
		// store the new time in the preferences file
		Editor editor = settings.edit();
		long time = (long) Math.floor(System.currentTimeMillis() / 1000); // unix time of now
		editor.putLong("lastLoadTime", time);
		editor.commit();
	}
	
	void sendMsg(Messenger client, int what, Bundle bundle) {
		try {
			// create a message according to parameters
			Message msg = Message.obtain(null, what);
			if (bundle != null) {
				msg.setData(bundle);
			}
			client.send(msg); // send the message
		} catch (RemoteException e) {
			// We are probably shutting down, but report it anyway
			Log.e("ERROR", "Unable to send message to client: " + e.getMessage());
		}
	}
	
	void sendMsg(int clientId, int what, Bundle bundle) {
		// simply call the main sendMessage but with an actual client
		sendMsg(clients.get(clientId), what, bundle);
	}
	
	void sendMsgToAll(int what, Bundle bundle) {
		// loop through and send the message to all the clients
		for (int i = 0; i < clients.size(); i++) {
			sendMsg(i, what, bundle);
		}
	}
	
	private void unregisterClient(Messenger client) {
		// remove our reference to the client
		clients.remove(client);
		// if we have no more clients and a load is not in progress, shutdown
		if(clients.isEmpty() && !loadInProgress) {
			stopSelf();
		}
	}
	
	boolean isOnline() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		// check that there is an active network
		if (info != null) {
			return info.isConnected();
		}
		else {
			return false;
		}
	}
	
	/**
	 * Called when an RSS feed has loaded
	 * 
	 * @param item
	 *            The item that has been loaded
	 */
	public synchronized void categoryRssLoaded(RSSItem[] items, String category) {
		// clear the priorities for this category to prevent old items hanging around
		database.clearPriorities(category);
		
		// insert the items into the database
		for (int i = 0; i < items.length; i++) {
			// check there are some thumbnails
			String thumbUrl = null;
			if (items[i].getThumbnails().size() == 2) {
				thumbUrl = items[i].getThumbnails().get(1).toString();
			}
			getDatabase().insertItem(items[i].getTitle(), items[i].getDescription(), category, items[i].getPubDate(), items[i].getLink().toString(),
					thumbUrl, i);
		}
		// send a message to the gui to tell it that we have loaded the category
		Bundle bundle = new Bundle();
		bundle.putString(KEY_CATEGORY, category);
		sendMsgToAll(MSG_CATEGORY_LOADED, bundle);
	}
	
	public synchronized void reportError(int type, String msg, String error) {
		// an error has occurred, send a message to the gui
		// this will display something useful to the user
		Bundle bundle = new Bundle();
		bundle.putInt(KEY_ERROR_TYPE, type);
		bundle.putString(KEY_ERROR_MESSAGE, msg);
		bundle.putString(KEY_ERROR_ERROR, error);
		sendMsgToAll(MSG_ERROR, bundle);
	}
	
	public synchronized void rssLoadComplete(boolean successful) {
		// check if the load was successful before continuing
		if (!successful) {
			fullLoadComplete(false); // end the load here, it was not successful
			return; // bail
		}
		
		updateLastLoadTime(); // save last load time
		// tell the gui
		sendMsgToAll(MSG_RSS_LOAD_COMPLETE, null);
		
		// add unloaded items to the download queue
		totalItemsToDownload = 0;
		itemsDownloaded = 0;
		
		// query the database to find out which items to load
		int itemLoadLimit = settings.getInt("itemLoadLimit", ReaderActivity.DEFAULT_ITEM_LOAD_LIMIT); // the limit for the number of items to load
		Integer[][] items = database.getUndownloaded(itemLoadLimit);
		
		// load the undownloaded articles
		Integer[] htmlIds = items[DatabaseHandler.COLUMN_UNDOWNLOADED_ARTICLES];
		for (int t = 0; t < htmlIds.length; t++) {
			String url = database.getUrl(htmlIds[t]);
			webManager.addToQueue(url, WebManager.ITEM_TYPE_HTML, htmlIds[t]);
		}
		// load the undownloaded thumbnails
		Integer[] thumbIds = items[DatabaseHandler.COLUMN_UNDOWNLOADED_ARTICLES];
		for (int t = 0; t < thumbIds.length; t++) {
			String url = database.getThumbnailUrl(thumbIds[t]);
			// check if there is a thumbnail url, if so load it
			if (url == null) {
				database.addThumbnail(thumbIds[t], ReaderActivity.NO_THUMBNAIL_URL_CODE);// Set thumbnail to no thumbnail
				// report that the thumbnail has been loaded so it can be displayed
				Bundle bundle = new Bundle();
				bundle.putInt("id", thumbIds[t]);
				sendMsgToAll(MSG_THUMB_LOADED, bundle);
			}
			else {
				webManager.addToQueue(url, WebManager.ITEM_TYPE_THUMB, thumbIds[t]);
			}
		}
		
		// set the items to download
		totalItemsToDownload = htmlIds.length + thumbIds.length;
		reportItemsToDownload();
		
		// if we didn't have to add anything, report the load as fully complete
		if (webManager.isQueueEmpty()) {
			fullLoadComplete(true);
		}
		
		// update the widget, if the load was successful
		if (successful) {
			AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
			ComponentName provider = new ComponentName(this, ReaderWidget.class);
			int[] ids = widgetManager.getAppWidgetIds(provider);
			// only broadcast an update request if there are some active widgets
			if (ids.length > 0) {
				Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
				intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
				sendBroadcast(intent);
			}
		}
	}
	
	public synchronized void fullLoadComplete(boolean successful) {
		// set the flag to false
		loadInProgress = false;
		
		// if we have clients, tell them the load is complete, else shutdown
		if(!clients.isEmpty()) {
			// send a message saying that we have loaded
			sendMsgToAll(MSG_FULL_LOAD_COMPLETE, null);
		} else {
			stopSelf();
		}
	}
	
	public synchronized void itemDownloadComplete(boolean specific, int itemId, int type, Object download) {
		// choose what to do depending on the type of object
		if (type == WebManager.ITEM_TYPE_HTML) {
			byte[] html = (byte[]) download;
			database.addHtml(itemId, html);
			// if this item was specifically requested we need to report that it has been loaded
			if (specific) {
				Bundle bundle = new Bundle();
				bundle.putInt(KEY_ITEM_ID, itemId);
				sendMsgToAll(MSG_ARTICLE_LOADED, bundle); // tell every client about the load
			}
		}
		if (type == WebManager.ITEM_TYPE_IMAGE) {
			byte[] image = (byte[]) download;
			database.addImage(itemId, image);
			// report that this thumbnail has been loading to the ui
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_ITEM_ID, itemId);
			sendMsgToAll(MSG_THUMB_LOADED, bundle);
		}
		if (type == WebManager.ITEM_TYPE_THUMB) {
			byte[] thumb = (byte[]) download;
			database.addThumbnail(itemId, thumb);
			// report that the thumbnail has been loaded so it can be displayed
			Bundle bundle = new Bundle();
			bundle.putInt(KEY_ITEM_ID, itemId);
			sendMsgToAll(MSG_THUMB_LOADED, bundle);
		}
		
		if (!specific) {
			// increment the number of items that have been loaded
			incrementItemsToDownload();
		}
	}
	
	void reportItemsToDownload() {
		// check if a load is in progress before sending this signal
		if (loadInProgress) {
			Bundle bundle = new Bundle();
			bundle.putInt("totalItems", totalItemsToDownload);
			bundle.putInt("itemsDownloaded", itemsDownloaded);
			sendMsgToAll(MSG_UPDATE_LOAD_PROGRESS, bundle);
		}
	}
	
	void incrementItemsToDownload() {
		itemsDownloaded++;
		reportItemsToDownload();
	}
	
	void updateSettings() {
		// get the alarm manager to allow triggering of loads in the future
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		
		// produce the intent to trigger a load
		Intent intent = new Intent(ACTION_LOAD);
		PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		// register alarms for background loading
		if (settings.getBoolean(ReaderActivity.PREFKEY_LOAD_IN_BACKGROUND, ReaderActivity.DEFAULT_LOAD_IN_BACKGROUND)) {
			// background loading is switched on, register an alarm to trigger loads, first work out the interval
			String loadIntervalString = settings.getString(ReaderActivity.PREFKEY_LOAD_INTERVAL, ReaderActivity.DEFAULT_LOAD_INTERVAL);
			long loadInterval;
			if (loadIntervalString.equals("15_mins")) {
				loadInterval = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
			}
			else if (loadIntervalString.equals("30_mins")) {
				loadInterval = AlarmManager.INTERVAL_HALF_HOUR;
			}
			else if (loadIntervalString.equals("1_hour")) {
				loadInterval = AlarmManager.INTERVAL_HOUR;
			}
			else if (loadIntervalString.equals("half_day")) {
				loadInterval = AlarmManager.INTERVAL_HALF_DAY;
			}
			else {
				loadInterval = AlarmManager.INTERVAL_HOUR;
			}
			
			// work out the starting time of the alarm
			long startingTime = System.currentTimeMillis() + loadInterval; // now plus the interval
			
			// register an alarm to start loads, depending on rtc wakeup
			if (settings.getBoolean(ReaderActivity.PREFKEY_RTC_WAKEUP, ReaderActivity.DEFAULT_RTC_WAKEUP)) {
				alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, startingTime, loadInterval, pendingIntent);
			}
			else {
				alarmManager.setInexactRepeating(AlarmManager.RTC, startingTime, loadInterval, pendingIntent);
			}
		}
		else {
			// background loading is switched off, cancel alarms
			alarmManager.cancel(pendingIntent);
		}
	}
	
	@Override
	public void onCreate() {
		// init variables
		loadInProgress = false;
		
		// load various key components
		if (settings == null) {
			// load in the settings
			settings = getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, MODE_PRIVATE); // load settings in read/write form
		}
		if (database == null) {
			// load the database
			setDatabase(new DatabaseHandler(this));
			// create tables in the database if needed
			if (!getDatabase().isCreated()) {
				getDatabase().addCategoriesFromXml();
			}
		}
		if (getWebManager() == null) {
			// load the web manager
			setWebManager(new WebManager(this));
		}
		if (rssManager == null) {
			// load the rss manager
			rssManager = new RSSManager(this);
		}
		
		// register to receive alerts when a load is required
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals("com.digitallizard.bbcnewsreader.action.LOAD_NEWS")) {
					loadData(); // load the news
				}
			}
		};
		this.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_LOAD));
		
		// load in the settings
		updateSettings();
		
		// register a change listener on the settings
		settingsChangedListener = new OnSharedPreferenceChangeListener() {
			public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
				// update the settings
				updateSettings();
			}
		};
		settings.registerOnSharedPreferenceChangeListener(settingsChangedListener);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// check if we have been told to load news on start
		if (intent != null) {
			if (intent.getAction().equals(ACTION_LOAD)) {
				// start a load
				loadData();
			}
		}
		
		// we want to continue running until explicitly stopped, so return sticky.
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		// unregister receivers
		this.unregisterReceiver(broadcastReceiver);
		if (settings != null && settingsChangedListener != null) {
			settings.unregisterOnSharedPreferenceChangeListener(settingsChangedListener);
		}
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
}
