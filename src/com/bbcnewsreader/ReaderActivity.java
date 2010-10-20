package com.bbcnewsreader;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ScrollView;

public class ReaderActivity extends Activity {
	/** variables */
	ScrollView scroller;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //scroller = (ScrollView)findViewById("news_scroller");
    }
}