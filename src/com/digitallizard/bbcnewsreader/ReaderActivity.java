/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;



public class ReaderActivity extends Activity {
	
	/* constants */
	static final int ACTIVITY_CHOOSE_CATEGORIES = 1;
	static final int CATEGORY_ROW_LENGTH = 4;
	static final int DIALOG_ERROR = 0;
	static final int NEWS_ITEM_DP_WIDTH = 100; //FIXME item width shouldn't be predefined
	static final String PREFS_FILE_NAME = "com.digitallizard.bbcnewsreader_preferences";
	static final int DEFAULT_LOAD_TO_DAYS = 1;
	static final int DEFAULT_CLEAR_OUT_AGE = 4;
	static final boolean DEFAULT_LOAD_IN_BACKGROUND = true;
	static final boolean DEFAULT_RTC_WAKEUP = true;
	
	/* variables */
	ScrollView scroller;

	private Messenger resourceMessenger;
	boolean resourceServiceBound;
	boolean loadInProgress;
	private DatabaseHandler database;
	private LayoutInflater inflater; //used to create objects from the XML
	private SharedPreferences settings; //used to save and load preferences
	Button refreshButton;
	TextView statusText;
	String[] categoryNames;
	ArrayList<TableLayout> physicalCategories;
	ItemLayout[][] physicalItems;
	int categoryRowLength; //the number of items to show per row
	Dialog errorDialog;
	boolean errorWasFatal;
	HashMap<String, Integer> itemIds;
	long lastLoadTime;

	/* service configuration */
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case ResourceService.MSG_CLIENT_REGISTERED:
		        //start a load if we haven't loaded within half an hour
		        //TODO make the load time configurable
				long difference = System.currentTimeMillis() - (lastLoadTime * 1000); //the time since the last load
				if(lastLoadTime == 0 || difference > (30 * 60 * 1000)){
					loadData(); //trigger a load
				}
				break;
			case ResourceService.MSG_ERROR:
				Bundle bundle = msg.getData(); //retrieve the data
				errorOccured(bundle.getBoolean("fatal"), bundle.getString("msg"), bundle.getString("error"));
				break;
			case ResourceService.MSG_NO_INTERNET:
				//tell the user that there is no internet connection
				showErrorDialog("There is no internet connection.\nThe news cannot be updated.");
				break;
			case ResourceService.MSG_CATEOGRY_LOADED:
				categoryLoadFinished(msg.getData().getString("category"));
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
			case ResourceService.MSG_THUMB_LOADED:
				thumbLoadComplete(msg.getData().getInt("id", 0));
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
    
    void errorOccured(boolean fatal, String msg, String error){
    	errorWasFatal = fatal; //so we know if we need to crash or not
    	//do we need to crash or not
    	if(fatal){
    		showErrorDialog("Fatal error:\n"+msg+"\nPlease try resetting the app.");
    		Log.e("BBC News Reader", "Fatal error: "+msg);
    		Log.e("BBC News Reader", error);
    	}
    	else{
    		showErrorDialog("Error:\n"+msg);
    		Log.e("BBC News Reader", "Error: "+msg);
        	Log.e("BBC News Reader", error);
    	}
    }
    
    void showErrorDialog(String error){
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
    
    void closeErrorDialog(){
    	errorDialog = null; //destroy the dialog
    	//see if we need to end the program
    	if(errorWasFatal){
    		//crash out
    		Log.e("BBC News Reader", "Oops something broke. We'll crash now.");
        	System.exit(1); //closes the app with an error code
    	}
    }
    
    void setLastLoadTime(long time){
		lastLoadTime = time; //store the time
    	//display the new time to the user
    	//check if the time is set
    	if(!loadInProgress){
	    	if(lastLoadTime == 0){
	    		//say we have never loaded
	    		statusText.setText("Last updated never");
	    	}
	    	else{
	    		//set the text to show date and time
	    		String status = "Last updated ";
	    		//find out time since last load in milliseconds
	    		long difference = System.currentTimeMillis() - (time * 1000); //the time since the last load
	    		//if within 1 hour, display minutes
	    		if(difference < (1000 * 60 * 60)){
	    			int minutesAgo = (int)Math.floor((difference / 1000) / 60);
	    			if(minutesAgo == 0)
	    				status += "just now";
	    			else if(minutesAgo == 1)
	    				status += minutesAgo + " minute ago";
	    			else
	    				status += minutesAgo + " minutes ago";
	    		}
	    		else{
	    			//if we are within 24 hours, display hours
	    			if(difference < (1000 * 60 * 60 * 24)){
	        			int hoursAgo = (int)Math.floor(((difference / 1000) / 60) / 60);
	        			if(hoursAgo == 1)
	        				status += hoursAgo + " hour ago";
	        			else
	        				status += hoursAgo + " hours ago";
	        		}
	    			else{
	    				//if we are within 2 days, display yesterday
	    				if(difference < (1000 * 60 * 60 * 48)){
	            			status += "yesterday";
	            		}
	    				else{
	    					//we have not updated recently
	    					status += "ages ago";
	    					//TODO more formal message?
	    				}
	    			}
	    		}
				statusText.setText(status);
	    	}
    	}
    }
    
    void loadBegun(){
    	loadInProgress = true; //flag the data as being loaded
    	//show the loading image on the button
    	refreshButton.setText("stop");
    	//tell the user what is going on
    	statusText.setText("Loading feeds...");
    }
    
    void loadData(){
    	//check we aren't currently loading news
    	if(!loadInProgress){
	    	//TODO display old news as old
	    	//tell the service to load the data
	    	sendMessageToService(ResourceService.MSG_LOAD_DATA);
    	}
    }
    
    void stopDataLoad(){
    	//check we are actually loading news
    	if(loadInProgress){
    		//send a message to the service to stop it loading the data
    		sendMessageToService(ResourceService.MSG_STOP_DATA_LOAD);
    	}
    }
    
    void fullLoadComplete(){
    	//check we are actually loading news
    	if(loadInProgress){
	    	loadInProgress = false;
	    	//display the reloading image on the button
	    	refreshButton.setText("reload");
	    	//report the loaded status
	    	setLastLoadTime(settings.getLong("lastLoadTime", 0)); //set the time as unix time
	    	//tell the database to delete old items
	    	database.clearOld();
    	}
    }
    
    void rssLoadComplete(){
    	//check we are actually loading news
    	if(loadInProgress){
    		//tell the user what is going on
    		statusText.setText("Loading article texts...");
    	}
    }
    
    void thumbLoadComplete(int id){
    	//loop through categories
    	for(int i = 0; i < physicalItems.length; i++){
    		for(int t = 0; t < physicalItems[i].length; t++){
    			if(physicalItems[i][t].getId() == id){
    				//try and get an image for this item
    				byte[] imageBytes = database.getThumbnail(id);
    				//check if any image data was returned
    				if(imageBytes != null){
    					//try to construct an image out of the bytes given by the database
    					Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); //load the image into a bitmap
    					physicalItems[i][t].setImage(imageBitmap);
    				}
    				else{
    					//set the image to the default "X"
    					physicalItems[i][t].setImage(R.drawable.no_thumb);
    				}
    			}
    		}
    	}
    }
    
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
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        loadInProgress = false;
        lastLoadTime = 0;
        
        //set up the inflater to allow us to construct layouts from the raw XML code
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        //make references to ui items
        refreshButton = (Button) findViewById(R.id.refreshButton);
        statusText = (TextView) findViewById(R.id.statusText);
        
        //load the preferences system
        settings = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE); //load settings in read/write form
        loadSettings(); //load in the settings
        
        //load the database
        database = new DatabaseHandler(this, settings.getInt("clearOutAge", DEFAULT_CLEAR_OUT_AGE));
        if(!database.isCreated()){
        	database.createTables();
        	database.addCategories();
        }
        
        createNewsDisplay();
        
        //start the service
        doBindService(); //loads the service
    }
    
    public void onResume(){
    	super.onResume(); //call the super class method
    	//update the last loaded display
    	setLastLoadTime(lastLoadTime);
    	//TODO update display more often?
    }
    
    void loadSettings(){
    	//check the settings file exists
    	if(settings != null){
	    	//load values from the settings
	    	setLastLoadTime(settings.getLong("lastLoadTime", 0)); //sets to zero if not in preferences
    	}
    }
    
    void createNewsDisplay(){
    	LinearLayout content = (LinearLayout)findViewById(R.id.newsScrollerContent); //a reference to the layout where we put the news
    	//clear the content area
    	content.removeAllViewsInLayout();
    	
    	//find the width and work out how many items we can add
    	int rowPixelWidth = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
    	int rowWidth =  (int)Math.floor(rowPixelWidth / this.getResources().getDisplayMetrics().density); //formula to convert from pixels to dp
    	categoryRowLength = (int)Math.floor(rowWidth / NEWS_ITEM_DP_WIDTH);
    	
        //create the categories
        categoryNames = database.getEnabledCategories()[1]; //string array with category names in it
        physicalCategories = new ArrayList<TableLayout>(categoryNames.length);
        physicalItems = new ItemLayout[categoryNames.length][CATEGORY_ROW_LENGTH]; //the array to hold the news items
        physicalItems = new ItemLayout[categoryNames.length][categoryRowLength]; //the array to hold the news items
        itemIds = new HashMap<String, Integer>();
        //loop through adding category views
        for(int i = 0; i < categoryNames.length; i++){
        	//create the category
        	TableLayout category = (TableLayout)inflater.inflate(R.layout.list_category_item, null);
        	//change the name
        	TextView name = (TextView)category.findViewById(R.id.textCategoryName);
        	name.setText(categoryNames[i]);
        	//set the column span of the name to fit the width of the table
        	LayoutParams layout = (LayoutParams) name.getLayoutParams();
        	layout.span = categoryRowLength - 1;
        	//retrieve the row for the news items
        	TableRow newsRow = (TableRow)category.findViewById(R.id.rowNewsItem);
        	
        	//add some items to each category display
        	//loop through and add x physical news items
        	for(int t = 0; t < categoryRowLength; t++){
        		//add a new item to the display
        		ItemLayout item = (ItemLayout)inflater.inflate(R.layout.list_news_item, null);
        		physicalItems[i][t] = item; //store the item for future use
        		newsRow.addView(item); //add the item to the display
        	}
        	physicalCategories.add(i, category); //store the category for future use
        	content.addView(category); //add the category to the screen
        	
        	//populate this category with news
        	displayCategoryItems(i);
        }
    }
    
    void displayCategoryItems(int category){
    	//load from the database, if there's anything in it
    	String[][] items = database.getItems(categoryNames[category]);
    	if(items != null){
    		String[] titles = items[0];
    		String[] ids = items[3];
    		//change the physical items to match this
    		for(int i = 0; i < categoryRowLength; i++){
    			//check we have not gone out of range of the available news
    			if(i < titles.length){
    				physicalItems[category][i].setTitle(titles[i]);
    				physicalItems[category][i].setId(Integer.parseInt(ids[i]));
    				
    				//try and get an image for this item
    				byte[] imageBytes = database.getThumbnail(Integer.parseInt(ids[i]));
    				//check if any image data was returned
    				if(imageBytes != null){
    					//try to construct an image out of the bytes given by the database
    					Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); //load the image into a bitmap
    					physicalItems[category][i].setImage(imageBitmap);
    				}
    				else{
    					//set the image to the default "X"
    					physicalItems[category][i].setImage(R.drawable.no_thumb);
    				}
    			}
    		}
    	}
    }
    
    void categoryLoadFinished(String category){
    	//the database has finished loading a category, we can update
    	//FIXME very inefficient way to turn (string) name into (int) id
    	int id = 0; //the id of the client
    	for(int i = 0; i < categoryNames.length; i++){
    		//check if the name we have been given matches this category
    		if(category.equals(categoryNames[i]))
    			id = i;
    	}
    	displayCategoryItems(id); //redisplay this category
    }
    
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	//inflate the menu XML file
    	MenuInflater menuInflater = new MenuInflater(this);
    	menuInflater.inflate(R.layout.options_menu, menu);
    	return true; //we have made the menu so we can return true
    }
    
    protected void onDestroy(){
    	//disconnect the service
    	doUnbindService();
    	super.onDestroy(); //pass the destroy command to the super
    }
    
    public boolean onOptionsItemSelected(MenuItem item){
    	if(item.getTitle().equals("Choose Categories")){
    		//launch the category chooser activity
    		//create an intent to launch the next activity
        	Intent intent = new Intent(this, CategoryChooserActivity.class);
        	//load the boolean array of currently enabled categories
        	boolean[] categoryBooleans = database.getCategoryBooleans();
        	intent.putExtra("categorybooleans", categoryBooleans);
        	startActivityForResult(intent, ACTIVITY_CHOOSE_CATEGORIES);
    	}
    	if(item.getTitle().equals("Settings")){
    		//show the settings menu
    		Intent intent = new Intent(this, SettingsActivity.class);
    		startActivity(intent);
    	}
    	return true; //we have received the press so we can report true
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	//wait for activities to send us result data
    	switch(requestCode){
    	case ACTIVITY_CHOOSE_CATEGORIES:
    		//check the request was a success
    		if(resultCode == RESULT_OK){
    			//TODO store the data sent back
    			database.setEnabledCategories(data.getBooleanArrayExtra("categorybooleans"));
    			//reload the ui
    			createNewsDisplay();
    		}
    		break;
    	}
    }
    
    public void refreshClicked(View item){
    	//Log.v("view", "width is: "+physicalCategories[1]].getWidth());
    	//start the load if we are not loading
    	if(!loadInProgress)
    		loadData();
    	else
    		stopDataLoad();
    }
    
    public void itemClicked(View view){
    	//retrieve the title of this activity
    	ItemLayout item = (ItemLayout)view; //cast the view to a an itemlayout

    	//check there is an item at this view
    	if(item.isItem()){
    		//launch article view activity
    		Intent intent = new Intent(this, ArticleActivity.class);
	    	intent.putExtra("id", item.getId());
	    	startActivity(intent);
    	}
    }
    
    public void categoryClicked(View view){
    	//FIXME there must be a more elegant way of doing this...
    	//get the parent of this view
    	TableLayout category = (TableLayout)(view.getParent());
    	//find the id of this category by looking it up in the list
    	int id = physicalCategories.indexOf(category);
    	//launch a new activity to show this category
    	Intent intent = new Intent(this, CategoryActivity.class);
    	intent.putExtra("title", categoryNames[id]);
    	startActivity(intent);
    }
}
