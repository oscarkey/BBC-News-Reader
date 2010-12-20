package com.bbcnewsreader;

import org.mcsoxford.rss.RSSItem;


public interface ResourceInterface {
	public void categoryRssLoaded(RSSItem[] items, String category); //called when the RSS has loaded
	public void reportError(boolean fatal, String msg);
}
