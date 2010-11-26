package com.bbcnewsreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
	/** variables */
	/* constants */
	static final int ACTIVITY_CHOOSE_CATEGORIES = 1;
	static final int CATEGORY_ROW_LENGTH = 4;
	
	/* variables */
	ScrollView scroller;
	DatabaseHandler database;
	LayoutInflater inflater; //used to create objects from the XML
	String[] categoryNames;
	TableLayout[] categories;
	LinearLayout[] items;
	/*String[] itemNames = {"lorem", "ipsum", "dolor", "sit", "amet",
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
			"purus"};*/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //load the database
        database = new DatabaseHandler(this);
        //database.dropTables();
        //load in the categories if necessary
        database.addCategories();
        
        //set up the inflater to allow us to construct layouts from the raw XML code
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout content = (LinearLayout)findViewById(R.id.newsScrollerContent); //a reference to the layout where we put the news
        //create the categories
        categoryNames = database.getEnabledCategories(); //string array with category names in it
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
        	String[] itemTitles = database.getItems(categoryNames[i])[0];
        	//loop through and add 3 news items
        	for(int t = 0; t < CATEGORY_ROW_LENGTH; t++){
        		LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_news_item, null);
        		TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
        		title.setText(itemTitles[t]);
        		items[(i*CATEGORY_ROW_LENGTH)+t] = item;
        		newsRow.addView(item);
        	}
        	categories[i] = category;
        	content.addView(category); //add the category to the screen
        }
    }
    
    void reloadNewsItems(){
    	//TODO add news reload code
    }
    
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	//inflate the menu XML file
    	MenuInflater menuInflater = new MenuInflater(this);
    	menuInflater.inflate(R.layout.options_menu, menu);
    	return true; //we have made the menu so we can return true
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
    		}
    		break;
    	}
    }
    
    public void refreshClicked(View item){
    	//TODO refresh
    }
    
    public void itemClicked(View item){
    	//TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
    	//create an intent to launch the next activity
    	//TODO work out how to use an intent to tell the article activity what to display
    	Intent intent = new Intent(this, ArticleActivity.class);
    	startActivity(intent);
    }
}