package com.bbcnewsreader;

import java.util.List;

import com.bbcnewsreader.data.DataHelper;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ScrollView;

public class ReaderActivity extends Activity {
	/** variables */
	ScrollView scroller;
	private DataHelper dh;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //scroller = (ScrollView)findViewById("news_scroller");
        this.dh = new DataHelper(this);
        this.dh.insert("Porky Pig");
        this.dh.insert("Foghorn Leghorn");
        this.dh.insert("Yosemite Sam");
        List<String> names = this.dh.selectAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Names in database:\n");
        for (String name : names) {
        sb.append(name + "\n");
        }
        Log.d("EXAMPLE", "names size - " + names.size());

        
    }
}
