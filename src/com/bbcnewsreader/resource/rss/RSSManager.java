package com.bbcnewsreader.resource.rss;

import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;


public class RSSManager implements Runnable {
	Thread thread;
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
	
	public RSSManager(String[] urls){
		this.urls = urls; //store the URLS
		feeds = new ArrayList<RSSFeed>();
		thread = new Thread(this);
	}
	
	public void run(){
		//load in the feeds
		for(int i = 0; i < urls.length; i++){
			RSSFeed feed = getFeed(urls[i]);
			feeds.add(feed);
		}
		//as we have loaded feeds, report this
		
	}
}
