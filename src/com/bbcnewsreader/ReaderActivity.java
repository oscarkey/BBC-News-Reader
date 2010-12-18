package com.bbcnewsreader;


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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.bbcnewsreader.data.DatabaseHandler;



public class ReaderActivity extends Activity {
	
	/* constants */
	static final int ACTIVITY_CHOOSE_CATEGORIES = 1;
	static final int CATEGORY_ROW_LENGTH = 4;
	
	/* variables */
	ScrollView scroller;

	static final int rowLength = 4;

	private Messenger resourceMessenger;
	boolean resourceServiceBound;
	private DatabaseHandler database;
	LayoutInflater inflater; //used to create objects from the XML
	String[] categoryNames;
	TableLayout[] categories;
	LinearLayout[] items;
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
				errorOccured(msg.obj);
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
    
    void errorOccured(Object obj){
    	//check if fatal or not
    	String[] msgs = (String[])obj;
    	boolean fatal = Boolean.parseBoolean(msgs[0]);
    	String msg = msgs[1];
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
    	//TODO display old news as old
    	//tell the service to load the data
    	sendMessageToService(ResourceService.MSG_LOAD_DATA);
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
        
        //load the database
        database = new DatabaseHandler(this);
        if(!database.isCreated()){
        	database.createTables();
        	database.addCategories();
        }
        
        //set up the inflater to allow us to construct layouts from the raw XML code
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        createNewsDisplay();
    }
    
    void createNewsDisplay(){
    	LinearLayout content = (LinearLayout)findViewById(R.id.newsScrollerContent); //a reference to the layout where we put the news
    	//clear the content area
    	content.removeAllViewsInLayout();
        //create the categories
        categoryNames = getResources().getStringArray(R.array.category_names); //string array with category names in it
        categoryNames = database.getEnabledCategories()[1]; //string array with category names in it
        categories = new TableLayout[categoryNames.length];
        items = new LinearLayout[categoryNames.length * CATEGORY_ROW_LENGTH]; //the array to hold the news items
        //loop through adding category views
        for(int i = 0; i < categoryNames.length; i++){
        	//create the category
        	TableLayout category = (TableLayout)inflater.inflate(R.layout.list_category_item, null);
        	//change the name
        	TextView name = (TextView)category.findViewById(R.id.textCategoryName);
        	name.setText(categoryNames[i]);
        	//retrieve the row for the news items
        	TableRow newsRow = (TableRow)category.findViewById(R.id.rowNewsItem);
        	//load up the item titles
        	String[][] newsItems = database.getItems(categoryNames[i]);
        	Log.v(this.getLocalClassName(), "items: "+newsItems);
        	String[] itemTitles = null;
        	//check there are actually some news items
        	if(newsItems != null)
        		itemTitles = newsItems[0];
        	//String[] itemTitles = itemNames;
        	//FIXME deal with no or few items being returned
        	//loop through and add 4 news items
        	for(int t = 0; t < CATEGORY_ROW_LENGTH; t++){
        		LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_news_item, null);
        		TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
        		if(itemTitles != null){
        			if(t < itemTitles.length)
        				title.setText(itemTitles[t]);
        		}
        		items[(i*rowLength)+t] = item;
        		items[(i*CATEGORY_ROW_LENGTH)+t] = item;
        		newsRow.addView(item);
        	}
        	categories[i] = category;
        	content.addView(category); //add the category to the screen
        }
        
        //start the service and tell it to start to refresh XML data
        doBindService(); //loads the service
    }
    
    void reloadNewsItems(){
    	//TODO add news reload code
    	//TODO tell the database to discard old items
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
    	//TODO add code to show the settings menu
    	if(item.getTitle().equals("Settings")){
    		//TODO add code to show the settings menu
    	}
    	if(item.getTitle().equals("Reset")){
    		//clear the database tables and then crash out
    		//FIXME shouldn't crash on a table clear...
    		database.reset();
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
    	//TODO refresh
    	//reload the ui
    	createNewsDisplay();
    }
    
    public void itemClicked(View item){
    	//TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
    	//create an intent to launch the next activity
    	//TODO work out how to use an intent to tell the article activity what to display
    	Intent intent = new Intent(this, ArticleActivity.class);
    	startActivity(intent);
    }
}