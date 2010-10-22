package com.bbcnewsreader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.io.FeedException;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class ReaderActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.v("ERROR","TEST");
        ResourceManager manager=new ResourceManager();
        }
}