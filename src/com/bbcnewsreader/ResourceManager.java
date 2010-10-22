package com.bbcnewsreader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;

import com.bbcnewsreader.rss.RSSFeed;
import com.bbcnewsreader.rss.RSSHandler;
import com.bbcnewsreader.rss.RSSReader;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;

public class ResourceManager {
	// temp input data
		public final RSSFeed[] feeds=new RSSFeed[3];
		public final String[] feedUrls={"http://feeds.bbci.co.uk/news/technology/rss.xml","http://feeds.bbci.co.uk/news/science_and_environment/rss.xml","http://feeds.bbci.co.uk/news/world/latin_america/rss.xml"};
		
		public ResourceManager()
		{
			initializeFeeds();
		
		}
		private String[] getURLs()
		{
			//Dummy data fetch
			String[] feedURLs={"http://feeds.bbci.co.uk/news/technology/rss.xml","http://feeds.bbci.co.uk/news/science_and_environment/rss.xml","http://feeds.bbci.co.uk/news/world/latin_america/rss.xml"};
			return feedURLs;
		}
		private void initializeFeeds()
		{
			String[] feedURLs=getURLs();
			List<RSSFeed> feeds = null;
			for(int i=0;i<feedURLs.length;i++)
			{
				feeds.add(getFeed(feedUrls[i]));
			}
			//Data export here
		}
	    private RSSFeed getFeed(String urlToRssFeed)
	    {
	    	try
	    	{
	    		// setup the url
	    	   URL url = new URL(urlToRssFeed);

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
	           // get our data via the url class
	           InputSource is = new InputSource(url.openStream());
	           // perform the synchronous parse           
	           xmlreader.parse(is);
	           // get the results - should be a fully populated RSSFeed instance, or null on error
	           return theRssHandler.getFeed();
	    	}
	    	catch (Exception ee)
	    	{
	    		// if we have a problem, simply return null
	    		return null;
	    	}
	    }
}
