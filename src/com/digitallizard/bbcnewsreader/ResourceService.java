/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.mcsoxford.rss.RSSItem;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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

public class ResourceService extends Service implements ResourceInterface {
	/* variables */
	public boolean loadInProgress; //a flag to tell the activity if there is a load in progress
	ArrayList<Messenger> clients = new ArrayList<Messenger>(); //holds references to all of our clients
	final Messenger messenger = new Messenger(new IncomingHandler()); //the messenger used for communication
	BroadcastReceiver broadcastReceiver;
	DatabaseHandler database; //the database
	RSSManager rssManager;
	WebManager webManager;
	SharedPreferences settings;
		
	/* command definitions */
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_CLIENT_REGISTERED = 3; //returned to a client when registered
	static final int MSG_LOAD_DATA = 4; //sent to request a data load
	static final int MSG_LOAD_ARTICLE = 11;
	static final int MSG_LOAD_THUMB = 12;
	static final int MSG_LOAD_IMAGE = 13;
	static final int MSG_STOP_DATA_LOAD = 9; //sent to stop data loading
	static final int MSG_CATEOGRY_LOADED = 6; //sent when a category has loaded
	static final int MSG_ARTICLE_LOADED = 15; //article loaded
	static final int MSG_THUMB_LOADED = 14; //thumbnail loaded
	static final int MSG_NOW_LOADING = 16;
	static final int MSG_FULL_LOAD_COMPLETE = 8; //sent when all the data has been loaded
	static final int MSG_RSS_LOAD_COMPLETE = 10;
	static final int MSG_ERROR = 7; //help! An error occurred
	static final int MSG_NO_INTERNET = 17; //sent when the internet has failed
	static final String ACTION_LOAD = "com.digitallizard.bbcnewsreader.action.LOAD_NEWS";
	
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo); //add a reference to the client to our list
				sendMsg(msg.replyTo, MSG_CLIENT_REGISTERED, null);
				break;
			case MSG_UNREGISTER_CLIENT:
				Log.v("ResourceService", "unregister request recieved");
				clients.remove(msg.replyTo); //remove our reference to the client
				//FIXME when should the service shutdown?
				break;
			case MSG_LOAD_DATA:
				loadData(); //start of the loading of data
				break;
			case MSG_LOAD_ARTICLE:
				Log.v("service", "specific load requested");
				loadArticle(msg.getData().getInt("itemId"));
				break;
			case MSG_LOAD_THUMB:
				//TODO load specific thumb
				break;
			case MSG_LOAD_IMAGE:
				//TODO load specific image
				break;
			case MSG_STOP_DATA_LOAD:
				stopDataLoad();
				break;
			default:
				super.handleMessage(msg); //we don't know what to do, lets hope that the super class knows
			}
		}
	}
	
	public class ResourceBinder extends Binder {
		ResourceService getService(){
			return ResourceService.this;
		}
	}
	
	public synchronized void setDatabase(DatabaseHandler db){
		this.database = db;
	}
	
	public synchronized DatabaseHandler getDatabase(){
		return database;
	}
	
	public synchronized void setWebManager(WebManager manager){
		this.webManager = manager;
	}
	
	public synchronized WebManager getWebManager(){
		return this.webManager;
	}
	
	void loadData(){
		//check if the device is online
		if(isOnline()){
			//report to the gui that a load has been activated
			sendMsgToAll(MSG_NOW_LOADING, null);
			//set the flag saying that we are loading
			loadInProgress = true;
			//retrieve the active category urls
			String[] urls = getDatabase().getEnabledCategories()[0];
			//work out the names
			String[] names = new String[urls.length];
			String[] allNames = getResources().getStringArray(R.array.category_names);
			String[] allUrls = getResources().getStringArray(R.array.catergory_rss_urls);
			//FIXME very inefficient, should be done by database
			for(int i = 0; i < allUrls.length; i++){
				for(int j = 0; j < urls.length; j++){
					if(allUrls[i].equals(urls[j])){
						names[j] = allNames[i];
					}
				}
			}
			//start the RSS Manager
			rssManager.load(names, urls);
		}
		else{
			//report that there is no internet connection
			sendMsgToAll(MSG_NO_INTERNET, null);
		}
	}
	
	void loadArticle(int id){
		String url = database.getItem(id)[2]; //get the url of the item
		webManager.loadNow(url, WebManager.ITEM_TYPE_HTML, id); //tell the webmanager to load this
	}
	
	void loadThumb(int id){
		//TODO add specific thumbnail loading
	}
	
	void loadImage(int id){
		//TODO add specific image loading
	}
	
	void stopDataLoad(){
		//stop the data loading
		rssManager.stopLoading();
		getWebManager().stopDownload();
		//the stopping of loading will be reported by the managers...
	}
	
	void updateLastLoadTime(){
		//store the new time in the preferences file
		Editor editor = settings.edit();
		long time = (long)Math.floor(System.currentTimeMillis() / 1000); //unix time of now
		editor.putLong("lastLoadTime", time);
		editor.apply();
	}
	
	void sendMsg(Messenger client, int what, Bundle bundle){
		try{
			//create a message according to parameters
			Message msg = Message.obtain(null, what);
			if(bundle != null){
				msg.setData(bundle);
			}
			client.send(msg); //send the message
		}
		catch(RemoteException e){
			//We are probably shutting down, but report it anyway
			Log.e("ERROR", "Unable to send message to client: " + e.getMessage());
		}
	}
	
	void sendMsg(int clientId, int what, Bundle bundle){
		//simply call the main sendMessage but with an actual client
		sendMsg(clients.get(clientId), what, bundle);
	}
	
	void sendMsgToAll(int what, Bundle bundle){
		//loop through and send the message to all the clients
		for(int i = 0; i < clients.size(); i++){
			sendMsg(i, what, bundle);
		}
	}
	
	boolean isOnline(){
		ConnectivityManager manager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		//check that there is an active network
		if(info != null){
			return info.isConnected();
		}
		else{
			return false;
		}
	}
	
	/**
	 * Called when an RSS feed has loaded
	 * @param item The item that has been loaded */
	public synchronized void categoryRssLoaded(RSSItem[] items, String category){
		//insert the items into the database
		for(int i = 0; i < items.length; i++){
			//FIXME no description given
			//FIXME stupid conversion and reconversion of date format. The database needs updating.
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
			String date = dateFormat.format(items[i].getPubDate());
			//check there are some thumbnails
			String thumbUrl = null;
			if(items[i].getThumbnails().size() == 2)
				thumbUrl = items[i].getThumbnails().get(1).toString();
			getDatabase().insertItem(items[i].getTitle(), items[i].getDescription(), items[i].getLink().toString(), date, category, thumbUrl);
		}
		//send a message to the gui to tell it that we have loaded the category
		Bundle bundle = new Bundle();
		bundle.putString("category", category);
		sendMsgToAll(MSG_CATEOGRY_LOADED, bundle);
	}
	
	public synchronized void reportError(boolean fatal, String msg, String error){
		//an error has occurred, send a message to the gui
		//this will display something useful to the user
		Bundle bundle = new Bundle();
		bundle.putBoolean("fatal", fatal);
		bundle.putString("msg", msg);
		bundle.putString("error", error);
		sendMsgToAll(MSG_ERROR, bundle);
		if(!isOnline()){
			//if we are not online, this may be the cause of the error
			sendMsgToAll(MSG_NO_INTERNET, null);
		}
	}
	
	public synchronized void rssLoadComplete(boolean successful){
		//check if the load was successful before continuing
		if(successful){
			updateLastLoadTime(); //save last load time
			//tell the gui
			sendMsgToAll(MSG_RSS_LOAD_COMPLETE, null);
			//as the rss load has completed we can begin loading articles etc
			int loadToDays = settings.getInt("loadToDays", ReaderActivity.DEFAULT_LOAD_TO_DAYS); //find user preference
			Integer[][] items = database.getUndownloaded(loadToDays); //find stuff up to x days old
			//loop through and add articles to the queue
			for(int i = 0; i < items[0].length; i++){
				//FIXME should only get url, not whole item
				String url = database.getItem(items[0][i])[2];
				webManager.addToQueue(url, WebManager.ITEM_TYPE_HTML, items[0][i]);
				//FIXME inefficiencies with converting uri -> string and back
			}
			//loop through and add thumbnails to the queue
			for(int i = 0; i < items[1].length; i++){
				//FIXME should only get url, not whole item
				String url = database.getItem(items[0][i])[4];
				//check if there is a thumbnail url, if so load it
				if(url != null)
					webManager.addToQueue(url, WebManager.ITEM_TYPE_THUMB, items[1][i]);
			}
			//loop through and add images to the queue
			for(int i = 0; i < items[2].length; i++){
				//TODO support image loading
			}
			//if we didn't have to add anything, report the load as fully complete
			if(items[0].length == 0 && items[1].length == 0){
				fullLoadComplete(true);
			}
		}
		else{
			fullLoadComplete(false); //end the load here, it was not successful
		}
	}
	
	public synchronized void fullLoadComplete(boolean successful){
		//set the flag to false
		loadInProgress = false;
		//send a message saying that we have loaded
		sendMsgToAll(MSG_FULL_LOAD_COMPLETE, null);
	}
	
	public synchronized void itemDownloadComplete(boolean specific, int itemId, int type, Object download){
		//choose what to do depending on the type of object
		if(type == WebManager.ITEM_TYPE_HTML){
			String html = (String)download;
			database.addHtml(itemId, html);
		}
		if(type == WebManager.ITEM_TYPE_IMAGE){
			byte[] image = (byte[])download;
			database.addImage(itemId, image);
			//report that this thumbnail has been loading to the ui
			Bundle bundle = new Bundle();
			bundle.putInt("id", itemId);
			sendMsgToAll(MSG_THUMB_LOADED, bundle);
		}
		if(type == WebManager.ITEM_TYPE_THUMB){
			byte[] thumb = (byte[])download;
			database.addThumbnail(itemId, thumb);
			//report that the thumbnail has been loaded so it can be displayed
			Bundle bundle = new Bundle();
			bundle.putInt("id", itemId);
			sendMsgToAll(MSG_THUMB_LOADED, bundle);
		}
		//if this item was specifically requested we need to report that it has been loaded
		if(specific){
			//report that a specifically requested item has been loaded
			Bundle bundle = new Bundle();
			bundle.putInt("item", itemId);
			sendMsgToAll(MSG_ARTICLE_LOADED, bundle); //tell every client about the load
		}
	}
	
	@Override
	public void onCreate(){
		//init variables
		loadInProgress = false;
		
		//load various key components
		if(settings == null){
			//load in the settings
			settings = getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, MODE_PRIVATE); //load settings in read/write form
		}
		if(database == null){
			//load the database
			int clearOutAge = settings.getInt("clearOutAge", ReaderActivity.DEFAULT_CLEAR_OUT_AGE); //load user preference
			setDatabase(new DatabaseHandler(this, clearOutAge));
			//create tables in the database if needed
			if(!getDatabase().isCreated()){
				getDatabase().createTables();
				getDatabase().addCategories();
	        }
		}
		if(getWebManager() == null){
			//load the web manager
			setWebManager(new WebManager(this));
		}
		if(rssManager == null){
			//load the rss manager
			rssManager = new RSSManager(this);
		}
		
		//register to receive alerts when a load is required
		broadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if(intent.getAction().equals("com.digitallizard.bbcnewsreader.action.LOAD_NEWS")){
					Log.v("BBC News Reader Service", "News load requested.");
					loadData(); //load the news
				}
			}
		};
		this.registerReceiver(broadcastReceiver, new IntentFilter(ACTION_LOAD));
		
		//check the preferences in terms of background loading
		if(settings.getBoolean("loadInBackground", ReaderActivity.DEFAULT_LOAD_IN_BACKGROUND)){
			//register an alarm to go off and start loads
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, 30); //move the calendar to 30 minutes in the future
			Intent intent = new Intent(ACTION_LOAD);
			PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
			//check if rtc wakeup is on or not (load when phone is in standby)
			//TODO allow the user selection of a load interval
			if(settings.getBoolean("rtcWakeup", ReaderActivity.DEFAULT_RTC_WAKEUP))
				alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR, sender);
			else
				alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HALF_HOUR, sender);
		}
	}
	 
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //We want to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
	public void onDestroy(){
		Log.v("ResourceService", "service is shutting down");
		this.unregisterReceiver(broadcastReceiver);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
}