package com.bbcnewsreader;


import java.net.URI;

import java.util.HashMap;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bbcnewsreader.data.DatabaseHandler;
import com.bbcnewsreader.resource.web.HtmlParser;



public class ReaderActivity extends Activity {
	
	/* constants */
	static final int ACTIVITY_CHOOSE_CATEGORIES = 1;
	static final int CATEGORY_ROW_LENGTH = 4;
	
	/* variables */
	ScrollView scroller;

	private Messenger resourceMessenger;
	boolean resourceServiceBound;
	boolean loadInProgress;
	private DatabaseHandler database;
	LayoutInflater inflater; //used to create objects from the XML
	ImageButton refreshButton;
	String[] categoryNames;
	TableLayout[] physicalCategories;
	LinearLayout[][] physicalItems;
	HashMap<String, String> itemUrls;
	String[] itemNames = {"lorem", "ipsum", "dolor", "sit", "amet",
			"consectetuer", "adipiscing", "elit", "morbi", "vel",
			"ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus", "lorem", "ipsum", "dolor", "sit", "amet",
			"consectetuer", "adipiscing", "elit", "morbi", "vel",
			"ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus", "lorem", "ipsum", "dolor", "sit", "amet",
			"consectetuer", "adipiscing", "elit", "morbi", "vel",
			"ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus","ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus", "lorem", "ipsum", "dolor", "sit", "amet",
			"consectetuer", "adipiscing", "elit", "morbi", "vel",
			"ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus"};
	

	/* service configuration */
	//the handler class to process new messages
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg){
			//decide what to do with the message
			switch(msg.what){
			case ResourceService.MSG_CLIENT_REGISTERED :
				loadData(); //start of the loading of data
				break;
			case ResourceService.MSG_ERROR:
				Bundle bundle = msg.getData(); //retrieve the data
				errorOccured(bundle.getBoolean("fatal"), bundle.getString("error"));
				break;
			case ResourceService.MSG_CATEOGRY_LOADED:
				categoryLoadFinished(msg.getData().getString("category"));
				break;
			case ResourceService.MSG_LOAD_COMPLETE:
				loadComplete();
				break;
			default:
				super.handleMessage(msg); //we don't know what to do, lets hope that the super class knows
			}
		}
	}
	final Messenger messenger = new Messenger(new IncomingHandler()); //this is a target for the service to send messages to
	
	private ServiceConnection resourceServiceConnection = new ServiceConnection() {
	    public void onServiceConnected(ComponentName className, IBinder service) {
	    	Log.v(getLocalClassName(), "Service connected");
	        //this runs when the service connects
	    	//save a pointer to the service to a local variable
	        resourceMessenger = new Messenger(service);
	        //try and tell the service that we have connected
	        //this means it will keep talking to us
	        sendMessageToService(ResourceService.MSG_REGISTER_CLIENT_WITH_DATABASE, null);
	    }

	    public void onServiceDisconnected(ComponentName className) {
	        //this runs if the service randomly disconnects
	    	//if this happens there are more problems than a missing service
	        resourceMessenger = null; //as the service no longer exists, destroy its pointer
	    }
	};
    
    void errorOccured(boolean fatal, String msg){
    	//do we need to crash or not
    	if(fatal){
    		//TODO display sensible error message
    		Log.e("BBC News Reader", "Error: "+msg);
        	Log.e("BBC News Reader", "Oops something broke. We'll crash now.");
        	System.exit(1); //closes the app with an error code
    	}
    	else{
    		//TODO display sensible error message
    		Log.e("BBC News Reader", "Error: "+msg);
        	Log.e("BBC News Reader", "Oops something broke. Lets keep going.");
    	}
    }
    
    void loadData(){
    	//check we aren't currently loading news
    	if(!loadInProgress){
	    	//TODO display old news as old
	    	loadInProgress = true; //flag the data as being loaded
	    	//show the loading image on the button
	    	refreshButton.setImageDrawable(getResources().getDrawable(R.drawable.stop));
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
    
    void loadComplete(){
    	//check we are actually loading news
    	if(loadInProgress){
	    	loadInProgress = false;
	    	//display the reloading image on the button
	    	refreshButton.setImageDrawable(getResources().getDrawable(R.drawable.refresh));
	    	//tell the database to delete old items
	    	database.clearOld();
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
    
    void sendMessageToService(int what, Object object){
    	//check the service is bound before trying to send a message
    	if(resourceServiceBound){
	    	try{
				//create a message according to parameters
				Message msg = Message.obtain(null, what, object);
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
        
       //testrunning //FIXME Remove
        try{HtmlParser.getPage(new URI("http://www.bbc.co.uk/news/mobile/uk-england-11778873"));}
        catch(Exception e){System.out.println(e.toString());}
        
        loadInProgress = false;
        
        //load the database
        database = new DatabaseHandler(this);
        if(!database.isCreated()){
        	database.createTables();
        	database.addCategories();
        }
        
        //set up the inflater to allow us to construct layouts from the raw XML code
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        //make a reference to the refresh button
        refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        
        createNewsDisplay();
    }
    
    void createNewsDisplay(){
    	LinearLayout content = (LinearLayout)findViewById(R.id.newsScrollerContent); //a reference to the layout where we put the news
    	//clear the content area
    	content.removeAllViewsInLayout();
    	
        //create the categories
        categoryNames = database.getEnabledCategories()[1]; //string array with category names in it
        physicalCategories = new TableLayout[categoryNames.length];
        physicalItems = new LinearLayout[categoryNames.length][CATEGORY_ROW_LENGTH]; //the array to hold the news items
        itemUrls = new HashMap<String, String>();
        //loop through adding category views
        for(int i = 0; i < categoryNames.length; i++){
        	//create the category
        	TableLayout category = (TableLayout)inflater.inflate(R.layout.list_category_item, null);
        	//change the name
        	TextView name = (TextView)category.findViewById(R.id.textCategoryName);
        	name.setText(categoryNames[i]);
        	//retrieve the row for the news items
        	TableRow newsRow = (TableRow)category.findViewById(R.id.rowNewsItem);
        	
        	//add some items to each category display
        	//loop through and add 4 physical news items
        	for(int t = 0; t < CATEGORY_ROW_LENGTH; t++){
        		//add a new item to the display
        		LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_news_item, null);
        		physicalItems[i][t] = item; //store the item for future use
        		newsRow.addView(item); //add the item to the display
        	}
        	physicalCategories[i] = category; //store the category for future use
        	content.addView(category); //add the category to the screen
        	
        	//populate this category with news
        	displayCategoryItems(i);
        }
        
        //start the service and tell it to start to refresh XML data
        doBindService(); //loads the service
    }
    
    void displayCategoryItems(int category){
    	//load from the database, if there's anything in it
    	if(database.getItems(categoryNames[category]) != null){
    		String[] titles = database.getItems(categoryNames[category])[0];
    		String[] urls = database.getItems(categoryNames[category])[2];
    		//change the physical items to match this
    		for(int i = 0; i < CATEGORY_ROW_LENGTH; i++){
    			//check we have not gone out of range of the available news
    			if(i < titles.length){
    				TextView titleText = (TextView)physicalItems[category][i].findViewById(R.id.textNewsItemTitle);
    				titleText.setText(titles[i]);
    				//save the urls
    				itemUrls.put((String)titleText.getText(), urls[i]);
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
    
    protected void onDestory(){
    	super.onDestroy(); //pass the destroy command to the super
    	//disconnect the service
    	doUnbindService();
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
    		//TODO add code to show the settings menu
    		//TODO add a settings menu
    	}
    	if(item.getTitle().equals("Reset")){
    		//clear the database tables and then crash out
    		//FIXME shouldn't crash on a table clear...
    		database.dropTables();
    		Log.w(this.getLocalClassName(), "Tables dropped. The app will now crash...");
    		System.exit(0);
    	}
    	return true; //we have received the press so we can report true
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data){
    	Log.v(getLocalClassName(), "result received, code:"+resultCode);
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
    	//start the load if we are not loading
    	if(!loadInProgress)
    		loadData();
    	else
    		stopDataLoad();
    }
    
    public void itemClicked(View item){
    	//TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
    	//create an intent to launch the next activity
    	//TODO work out how to use an intent to tell the article activity what to display
    	Intent intent = new Intent(this, ArticleActivity.class);
    	TextView titleText = (TextView)item.findViewById(R.id.textNewsItemTitle);
    	intent.putExtra("url", (String)itemUrls.get(titleText.getText()));
    	//startActivity(intent);
    	
    	//TODO add a article view system to replace web view
    	WebView webView = new WebView(this);
		webView.loadUrl((String)itemUrls.get(titleText.getText()));
    }
}
