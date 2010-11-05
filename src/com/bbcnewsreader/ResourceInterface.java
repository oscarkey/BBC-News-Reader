package com.bbcnewsreader;

import com.bbcnewsreader.resource.rss.RSSItem;

public interface ResourceInterface {
	public void itemRssLoaded(RSSItem item, String category); //called when the RSS has loaded
	public void reportError(boolean fatal, String msg);
}
