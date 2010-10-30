package com.bbcnewsreader;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;

import com.bbcnewsreader.data.DatabaseHandler;

public class ReaderActivity extends Activity {
	/** variables */
	ScrollView scroller;
	private DatabaseHandler dh;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        this.dh = new DatabaseHandler(this);
        dh.dropTables();
        dh.insertItem("Title1", "description1", "link1", "Sat, 30 Oct 2010 09:26:07 GMT", "World");
        dh.insertItem("Title2", "description2", "link2", "Sat, 30 Oct 2010 08:35:00 GMT", "World");
        dh.insertItem("Title3", "description2", "link2", "Sat, 29 Sep 2010 11:01:19 GMT", "World");
        dh.insertCategory("World",true,"http://feeds.bbci.co.uk/world/rss.xml");
        dh.insertCategory("Technology",false,"http://feeds.bbci.co.uk/news/rss.xml");
        dh.insertCategory("Science",true,"http://feeds.bbci.co.uk/science/rss.xml");
        dh.clearOld();
        String[][] categories = dh.getItems("World");
        for(int i=0;i<categories[0].length;i++)
        {
        	Log.v("TEST",categories[0][i]);
        }
    }
}
