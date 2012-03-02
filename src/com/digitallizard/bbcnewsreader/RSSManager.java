/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.List;

import org.mcsoxford.rss.RSSException;
import org.mcsoxford.rss.RSSFault;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

public class RSSManager implements Runnable {
	/* constants */
	
	/* variables */
	ResourceInterface resourceInterface;
	Thread thread;
	String[] names;
	String[] urls;
	ArrayList<RSSFeed> feeds;
	RSSReader reader;
	boolean isLoading;
	volatile boolean noError;
	
	synchronized void setIsLoading(boolean keepLoading) {
		this.isLoading = keepLoading;
	}
	
	synchronized boolean isLoading() {
		return isLoading;
	}
	
	public RSSManager(ResourceInterface service) {
		this.resourceInterface = service;
	}
	
	public void load(String[] names, String[] urls) {
		// check we are not already loading
		if (!isLoading()) {
			this.names = names; // store the names
			this.urls = urls; // store the URLS
			feeds = new ArrayList<RSSFeed>();
			thread = new Thread(this);
			setIsLoading(true);
			noError = true;
			thread.start();
		}
	}
	
	public void stopLoading() {
		noError = false;
		setIsLoading(false);
	}
	
	public void run() {
		// create a reader
		reader = new RSSReader();
		// load in the feeds
		for (int i = 0; i < urls.length; i++) {
			// check we haven't been cancelled
			if (isLoading()) {
				RSSFeed feed;
				try {
					feed = reader.load(urls[i]);
					feeds.add(feed);
					List<RSSItem> items = feed.getItems();
					// loop through the items and send them to the parent service
					resourceInterface.categoryRssLoaded(items.toArray(new RSSItem[items.size()]), names[i]);
				} catch (RSSException e) {
					// FIXME not a good way to handle these errors, will be fixed by new RSSManager
					// report the error to the resource service
					resourceInterface.reportError(ReaderActivity.ERROR_TYPE_GENERAL, "The rss feed could not be read.", e.toString());
					// give up loading
					stopLoading();
				} catch (RSSFault e) {
					// FIXME not a good way to handle these errors, will be fixed by new RSSManager
					// report the error to the resource service
					resourceInterface.reportError(ReaderActivity.ERROR_TYPE_INTERNET,
							"The rss feed could not be read. Check your internet connection.", e.toString());
					// give up loading
					stopLoading();
				}
			}
		}
		// report that the load is complete
		resourceInterface.rssLoadComplete(noError);
		setIsLoading(false); // we are not longer loading
	}
}
