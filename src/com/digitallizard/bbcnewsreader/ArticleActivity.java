/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

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
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class ArticleActivity extends Activity {
	DatabaseHandler database;
	Messenger resourceMessenger;
	boolean resourceServiceBound;
	int id; //the article id
	LinearLayout layout;
	WebView webView;
	TextView textLoadingView;
	boolean loadNeeded; //true when we need to load something
	
	/* service configuration */
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case ResourceService.MSG_CLIENT_REGISTERED:
				//check if we need to load an article
				if(loadNeeded){
					//trigger a load
					loadData();
				}
				break;
			case ResourceService.MSG_ARTICLE_LOADED:
				//display the reloaded article
				String html = database.getHtml(id);
				displayArticle(html);
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
				Log.e("ERROR", "Unable to send message to service: " + e.getMessage());
			}
    	}
    }
    
    void sendMessageToService(int what){
    	sendMessageToService(what, null);
    }
    
    void loadData(){
    	Bundle bundle = new Bundle();
    	bundle.putInt("itemId", id);
    	sendMessageToService(ResourceService.MSG_LOAD_ARTICLE, bundle);
    }
    
    void displayArticle(String html){
    	webView.loadDataWithBaseURL(null, html, "text/html", "utf-8",null);
    	layout.removeAllViews();
    	layout.setGravity(Gravity.FILL); //make the webview fill the screen
		layout.addView(webView);
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	if(item.getTitle().equals("Reload Article")){
    		//reload the article
    		if(resourceServiceBound){
    			//display the load screen
    			layout.removeAllViews();
    			layout.setGravity(Gravity.CENTER);
    			layout.addView(textLoadingView);
    			loadData(); //trigger loading
    		}
    		else{
    			loadNeeded = true; //we need to load data
    			//start up the service
    			doBindService();
    		}
    	}
    	return true; //we have received the press so we can report true
    }
    
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	//inflate the menu XML file
    	MenuInflater menuInflater = new MenuInflater(this);
    	menuInflater.inflate(R.layout.article_options_menu, menu);
    	return true; //we have made the menu so we can return true
    }
    
    protected void onDestroy(){
    	//disconnect the service
    	doUnbindService();
    	database.onDestroy(); //shutdown the database
    	super.onDestroy(); //pass the destroy command to the super
    }
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article);
		loadNeeded = false;
		//create references to views
		layout = (LinearLayout)findViewById(R.id.articleLayout);
		textLoadingView = (TextView)findViewById(R.id.articleLoadingText);
		webView = new WebView(this); //create a web view to display the article
		//retrieve the article text from the database
		id = this.getIntent().getIntExtra("id", 0);
		database = new DatabaseHandler(this, 0); //we don't need to bother with the clear old date
		String html = database.getHtml(id);
		//check if any html was returned
		if(html != null){
			//display the article
			displayArticle(html);
		}
		else{
			//as the html was not returned, mark it as needing loading
			loadNeeded = true;
			//start up the service
			doBindService();
		}
	}
}