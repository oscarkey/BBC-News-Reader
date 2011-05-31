/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class CategoryActivity extends Activity {
	//HashMap<String, Integer> itemIds;
	ListView listView;
	DatabaseHandler database;
	ArrayList<NewsItem> items;
	Messenger resourceMessenger;
	boolean resourceServiceBound;

	
	/* service configuration */
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case ResourceService.MSG_THUMB_LOADED:
				thumbLoadComplete(msg.getData().getInt("id"));
				break;
			default:
				super.handleMessage(msg); //we don't know what to do, lets hope that the super class knows
			}
		}
	}
	final Messenger messenger = new Messenger(new IncomingHandler()); //this is a target for the service to send messages to
	
	private ServiceConnection resourceServiceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        //this runs when the service connects
	    	resourceServiceBound = true; //flag the service as bound
	    	//save a pointer to the service to a local variable
	        resourceMessenger = new Messenger(service);
	        //try and tell the service that we have connected
	        //this means it will keep talking to us
	        sendMessageToService(ResourceService.MSG_REGISTER_CLIENT, null);
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        //this runs if the service randomly disconnects
	    	//if this happens there are more problems than a missing service
	        resourceMessenger = null; //as the service no longer exists, destroy its pointer
	    }
	};
	
	void doBindService(){
    	//load the resource service
    	bindService(new Intent(this, ResourceService.class), resourceServiceConnection, Context.BIND_AUTO_CREATE);
    	resourceServiceBound = true;
    }
    
    void doUnbindService(){
    	//disconnect the resource service
    	//check if the service is bound, if so, disconnect it
    	if(resourceServiceBound){
    		//politely tell the service that we are disconnected
    		sendMessageToService(ResourceService.MSG_UNREGISTER_CLIENT);
    		//remove local references to the service
    		unbindService(resourceServiceConnection);
    		resourceServiceBound = false;
    	}
    }
    
    void sendMessageToService(int what, Bundle bundle){
    	//check the service is bound before trying to send a message
    	if(resourceServiceBound){
	    	try{
				//create a message according to parameters
				Message msg = Message.obtain(null, what);
				//add the bundle if needed
				if(bundle != null){
					msg.setData(bundle);
				}
				msg.replyTo = messenger; //tell the service to reply to us, if needed
				resourceMessenger.send(msg); //send the message
			}
			catch(RemoteException e){
				//We are probably shutting down, but report it anyway
				//Log.e("ERROR", "Unable to send message to service: " + e.getMessage());
			}
    	}
    }
    
    void sendMessageToService(int what){
    	sendMessageToService(what, null);
    }
    
    void thumbLoadComplete(int id){
    	//load the thumbnail
    	byte[] thumbnailBytes = database.getThumbnail(id);
    	//loop through and set this thumbnail
    	Iterator<NewsItem> iterator = items.iterator();
    	while(iterator.hasNext()){
    		NewsItem item = iterator.next();
    		if(item.getId() == id){
    			//set the thumbnail bytes
    			item.setThumbnailBytes(thumbnailBytes);
    		}
    	}
    	
    	//refresh this view in the list
    	((ItemAdapter)listView.getAdapter()).notifyDataSetChanged();
    }
	
	public void onDestroy(){
		//shutdown the database
		database.finish();
		//shutdown the list adapter
		((ItemAdapter)listView.getAdapter()).finish();
		//disconnect the service
		doUnbindService();
		
		super.onDestroy();
	}
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState); //create the parent class
		this.setContentView(R.layout.category); //load the layout
				
		listView = (ListView)this.findViewById(R.id.categoryListView);
		
		//set the title of this category
		String title = this.getIntent().getStringExtra("title"); //load title from the intent
		((TextView)findViewById(R.id.categoryTitle)).setText(title);
		
		//load in news items
		database = new DatabaseHandler(this, 0); //clear old date doesn't matter here
		items = new ArrayList<NewsItem>(Arrays.asList(database.getItems(title, 50))); //specify a high limit for the number of items
		listView.setAdapter(new ItemAdapter(this, R.layout.category_item, items));
		listView.setChoiceMode(ListView.CHOICE_MODE_NONE);
		
		//connect the service
		doBindService();
		
		//add a listener to detect clicks
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//retrieve the NewsItem at the index of the click
				NewsItem item = (NewsItem)parent.getAdapter().getItem(position);
				//launch an activity to view this item
				Intent intent = new Intent(parent.getContext(), ArticleActivity.class);
		    	intent.putExtra("id", item.getId());
		    	startActivity(intent);
			}
		});
		
		//add a listener to detect scrolls
		listView.setOnScrollListener(new OnScrollListener() {
			public void onScrollStateChanged(AbsListView list, int state) {
				//check to see if the user has stopped scrolling
				if(state == OnScrollListener.SCROLL_STATE_IDLE){
					//check to see if all the visible items have images
					int firstVisible = list.getFirstVisiblePosition();
					int lastVisible = list.getLastVisiblePosition();
					for(int i = firstVisible; i <= lastVisible; i++){
						NewsItem item = (NewsItem)list.getAdapter().getItem(i);
						//if this item doesn't have a thumbnail
						if(item.getThumbnailBytes() == null){
							//load the thumbnail
							Bundle bundle = new Bundle();
					    	bundle.putInt("itemId", item.getId());
					    	sendMessageToService(ResourceService.MSG_LOAD_THUMB, bundle);
						}
					}
				}
			}
			
			public void onScroll(AbsListView list, int firstVisible, int visibleItems, int totalItems) {
				//do nothing
			}
		});
	}
}
