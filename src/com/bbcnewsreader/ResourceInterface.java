package com.bbcnewsreader;

import com.bbcnewsreader.resource.rss.RSSItem;

public interface ResourceInterface {
	public void rssItemLoaded(RSSItem item, String category); //called when the RSS has loaded
}
