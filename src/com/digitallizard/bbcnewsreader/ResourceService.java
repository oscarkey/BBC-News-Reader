/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.mcsoxford.rss.RSSItem;
import com.digitallizard.bbcnewsreader.resource.web.WebManager;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class ResourceService extends Service implements ResourceInterface {
	/* variables */
	public boolean loadInProgress; //a flag to tell the activity if there is a load in progress
	ArrayList<Messenger> clients = new ArrayList<Messenger>(); //holds references to all of our clients
	final Messenger messenger = new Messenger(new IncomingHandler()); //the messenger used for communication
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
	static final int MSG_FULL_LOAD_COMPLETE = 8; //sent when all the data has been loaded
	static final int MSG_RSS_LOAD_COMPLETE = 10;
	static final int MSG_ERROR = 7; //help! An error occurred
	
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
			String thumbUrl = items[i].getThumbnails().get(0).toString();
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
		//print out the error for debuggers
		Log.e("ResourceService", "Error - fatal:"+fatal+" msg:"+msg+" error:"+error);
	}
	
	public synchronized void rssLoadComplete(){
		//tell the gui
		sendMsgToAll(MSG_RSS_LOAD_COMPLETE, null);
		//as the rss load has completed we can begin loading articles etc
		int loadToDays = settings.getInt("loadToDays", ReaderActivity.DEFAULT_LOAD_TO_DAYS); //find user preference
		Integer[][] items = database.getUndownloaded(loadToDays); //find stuff up to x days old
		Log.v("service", "items.length = "+items.length);
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
			String url = database.getItem(items[0][i])[3];
			webManager.addToQueue(url, WebManager.ITEM_TYPE_THUMB, items[1][i]);
		}
		//loop through and add images to the queue
		for(int i = 0; i < items[2].length; i++){
			//TODO support image loading
		}
		//if we didn't have to add anything, report the load as fully complete
		if(items[0].length == 0 && items[1].length == 0){
			fullLoadComplete();
		}
	}
	
	public synchronized void fullLoadComplete(){
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
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //We want to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
	public void onDestroy(){
		Log.v("ResourceService", "service is shutting down");
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}
}