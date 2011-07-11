/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSLoader;

public class RSSManager implements Runnable{
	/* constants */
	
	
	/* variables */
	ResourceInterface resourceInterface;
	Thread storeThread;
	String[] names;
	//String[] urls;
	//ArrayList<RSSFeed> feeds;
	//RSSReader reader;
	RSSLoader loader;
	volatile boolean isLoading;
	volatile boolean noError;
	
	public RSSManager(ResourceInterface service){
		this.resourceInterface = service;
	}
	
	public void load(String[] names, String[] uris){
		// check we are not already loading
		if(!isLoading){
			this.names = names;
			isLoading = true;
			noError = true;
			
			// schedule feeds for loading
			loader = RSSLoader.priority(uris.length); // initiate a new loader
			// loop through and add feeds to the queue
			for(int i = 0; i < uris.length; i++){
				loader.load(uris[i], uris.length - i);
			}
			
			// start a second thread to retrieve items from the loader and store them
			storeThread = new Thread(this);
			storeThread.start();
		}
	}
	
	public void stopLoading(){
		// stop the rss loader
		if(loader.isLoading()){
			loader.stop();
		}
		isLoading = false;
		noError = false;
	}
	
	public void run(){
		// retrieve the items as they are loaded
		for(int i = 0; loader.isLoading(); i++){
			try{
				Future<RSSFeed> future = loader.take();
				RSSFeed feed = future.get();
				List<RSSItem> items = feed.getItems();
				resourceInterface.categoryRssLoaded((RSSItem[])items.toArray(new RSSItem[items.size()]), names[i]);
			} catch(ExecutionException e){
				// mark the load as failed
				noError = false;
			} catch(InterruptedException e){
				// mark the load as failed
				noError = false;
			}
		}
		// report that the load is complete
		resourceInterface.rssLoadComplete(noError);
		isLoading = false;
	}
}
