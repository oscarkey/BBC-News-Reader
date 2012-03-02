/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import org.mcsoxford.rss.RSSItem;

public interface ResourceInterface {
	public void categoryRssLoaded(RSSItem[] items, String category); // called when the RSS has loaded
	
	public void reportError(int type, String msg, String error);
	
	public void rssLoadComplete(boolean successful);
	
	public void fullLoadComplete(boolean successful);
	
	public void itemDownloadComplete(boolean specific, int itemId, int type, Object download);
}
