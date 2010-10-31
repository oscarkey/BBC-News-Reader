package com.bbcnewsreader.resource.rss;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.bbcnewsreader.ResourceInterface;


public class RSSManager implements Runnable {
	/* constants */
	
	
	/* variables */
	ResourceInterface resourceInterface;
	Thread thread;
	String[] names;
	String[] urls;
	ArrayList<RSSFeed> feeds;
	
	RSSFeed getFeed(String feedUrl){
		try{
			// setup the URL
			URL url = new URL(feedUrl);
			// create the factory
			SAXParserFactory factory = SAXParserFactory.newInstance();
			// create a parser
			SAXParser parser = factory.newSAXParser();
			// create the reader (scanner)
			XMLReader xmlreader = parser.getXMLReader();
			// instantiate our handler
			RSSHandler theRssHandler = new RSSHandler();
			// assign our handler
			xmlreader.setContentHandler(theRssHandler);
			// get our data via the URL class
			InputSource is = new InputSource(url.openStream());
			// perform the synchronous parse           
			xmlreader.parse(is);
			// get the results - should be a fully populated RSSFeed instance, or null on error
			return theRssHandler.getFeed();
    	}
    	catch (Exception e){
    		// if we have a problem, simply return null, this will be caught further up
    		return null;
    	}
	}
	
	public RSSManager(String[] names, String[] urls, ResourceInterface service){
		this.names = names; //store the names
		this.urls = urls; //store the URLS
		feeds = new ArrayList<RSSFeed>();
		resourceInterface = service;
		thread = new Thread(this);
		thread.start();
	}
	
	public void run(){
		//load in the feeds
		for(int i = 0; i < urls.length; i++){
			RSSFeed feed = getFeed(urls[i]);
			feeds.add(feed);
			List<RSSItem> items = feed.getAllItems();
			//loop through the items and send them to the parent service
			//FIXME don't just assume that we want three items
			for(int t = 0; t < 3; t++){
				resourceInterface.itemRssLoaded(items.get(t), names[i]);
			}
		}
	}
}
