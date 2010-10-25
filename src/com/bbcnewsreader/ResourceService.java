package com.bbcnewsreader;

import java.util.ArrayList;

import com.bbcnewsreader.resource.rss.RSSItem;
import com.bbcnewsreader.resource.rss.RSSManager;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class ResourceService extends Service implements ResourceInterface {
	/* variables */
	ArrayList<Messenger> clients = new ArrayList<Messenger>(); //holds references to all of our clients
	final Messenger messenger = new Messenger(new IncomingHandler()); //the messenger used for communication
	RSSManager rssManager;
	
	/* command definitions */
	static final int MSG_REGISTER_CLIENT = 1;
	static final int MSG_UNREGISTER_CLIENT = 2;
	static final int MSG_CLIENT_REGISTERED = 3; //returned to a client when registered
	static final int MSG_LOAD_DATA = 4; //sent to request a data load
	
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case MSG_REGISTER_CLIENT:
				clients.add(msg.replyTo); //add a reference to the client to our list
				sendMsg(msg.replyTo, MSG_CLIENT_REGISTERED);
				break;
			case(MSG_UNREGISTER_CLIENT):
				clients.remove(msg.replyTo); //remove our reference to the client
			case(MSG_LOAD_DATA):
				loadData(); //start of the loading of data
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
	
	void loadData(){
		//TODO retrieve the active categories
		//for now just load in all of them
		String[] names = getResources().getStringArray(R.array.category_names);
		String[] urls = getResources().getStringArray(R.array.catergory_rss_urls);
		//start the RSS Manager
		rssManager = new RSSManager(names, urls);
	}
	
	void sendMsg(Messenger client, int what){
		try{
			//create a message according to parameters
			Message msg = Message.obtain(null, what);
			client.send(msg); //send the message
		}
		catch(RemoteException e){
			//We are probably shutting down, but report it anyway
			Log.e("ERROR", "Unable to send message to client: " + e.getMessage());
		}
	}
	
	void sendMsg(int clientId, int what, Object object){
		//simply call the main sendMessage but with an actual client
		sendMsg(clients.get(clientId), what);
	}
	
	/**
	 * Called when an RSS feed has loaded
	 * @param item The item that has been loaded */
	public void rssItemLoaded(RSSItem item, String category){
		//TODO add the item in the database
		
	}
	
	@Override
	public void onCreate(){
		
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //We want to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }
	
	@Override
	public void onDestroy(){
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return messenger.getBinder();
	}

}