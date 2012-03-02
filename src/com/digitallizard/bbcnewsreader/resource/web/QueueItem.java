/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.resource.web;

public class QueueItem implements Comparable<QueueItem> {
	public static final int PRIORITY_DOWNLOAD_NOW = 5; // the priority if instant download is needed
	private String url;
	private int type;
	private int itemId;
	private int priority;
	
	public QueueItem(String url, int type, int itemId, int priority) {
		this.url = url;
		this.type = type;
		this.itemId = itemId;
		this.priority = priority;
	}
	
	public int compareTo(QueueItem item) {
		if (this.priority < item.getPriority()) {
			return 1;
		}
		else if (this.priority > item.getPriority()) {
			return -1;
		}
		else {
			return 0;
		}
	}
	
	public int getType() {
		return type;
	}
	
	public String getUrl() {
		return url;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public boolean wasSpecificallyRequested() {
		if (priority == PRIORITY_DOWNLOAD_NOW) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public int getPriority() {
		return this.priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
}
