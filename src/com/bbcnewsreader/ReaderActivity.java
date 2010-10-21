package com.bbcnewsreader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ReaderActivity extends Activity {
	/** variables */
	LayoutInflater inflater; //used to create objects from the XML
	String[] categoryNames;
	TableLayout[] categories;
	String[] items = {"lorem", "ipsum", "dolor", "sit", "amet",
			"consectetuer", "adipiscing", "elit", "morbi", "vel",
			"ligula", "vitae", "arcu", "aliquet", "mollis",
			"etiam", "vel", "erat", "placerat", "ante",
			"porttitor", "sodales", "pellentesque", "augue",
			"purus"};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //set up the inflater to allow us to construct layouts from the raw XML code
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout content = (LinearLayout)findViewById(R.id.newsScrollerContent); //a reference to the layout where we put the news
        //create the categories
        categoryNames = getResources().getStringArray(R.array.category_names); //string array with category names in it
        categories = new TableLayout[categoryNames.length];
        //loop through adding category views
        for(int i = 0; i < categoryNames.length; i++){
        	//create the category
        	TableLayout category = (TableLayout)inflater.inflate(R.layout.list_category_item, null);
        	//change the name
        	TextView name = (TextView)category.findViewById(R.id.textCategoryName);
        	name.setText(categoryNames[i]);
        	//retrieve the row for the news items
        	TableRow newsRow = (TableRow)category.findViewById(R.id.rowNewsItem);
        	//loop through and add 3 news items
        	for(int t = 0; t < 3; t++){
        		LinearLayout item = (LinearLayout)inflater.inflate(R.layout.list_news_item, null);
        		TextView title = (TextView)item.findViewById(R.id.textNewsItemTitle);
        		title.setText(items[i+t]);
        		newsRow.addView(item);
        	}
        	categories[i] = category;
        	content.addView(category); //add the category to the screen
        }
    }
}