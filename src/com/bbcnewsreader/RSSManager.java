package com.bbcnewsreader;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;



public class RSSManager implements Runnable {
	/* constants */
	
	
	/* variables */
	ResourceInterface resourceInterface;
	Thread thread;
	String[] names;
	String[] urls;
	ArrayList<RSSFeed> feeds;
	RSSReader reader;
	
	public RSSManager(String[] names, String[] urls, ResourceInterface service){
		this.names = names; //store the names
		this.urls = urls; //store the URLS
		feeds = new ArrayList<RSSFeed>();
		resourceInterface = service;
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		//create a reader
		reader = new RSSReader();
		//load in the feeds
		for(int i = 0; i < urls.length; i++){
			RSSFeed feed;
			try {
				feed = reader.load(new URI(urls[i]));
				feeds.add(feed);
				List<RSSItem> items = (List<RSSItem>) feed.getItems();
				//loop through the items and send them to the parent service
				resourceInterface.categoryRssLoaded((RSSItem[])items.toArray(new RSSItem[0]), names[i]);
			} catch (RSSReaderException e) {
				resourceInterface.reportError(true, e.getMessage()); //report a fatal error
			} catch (URISyntaxException e) {
				resourceInterface.reportError(true, e.getMessage());
			}
		}
	}
}
