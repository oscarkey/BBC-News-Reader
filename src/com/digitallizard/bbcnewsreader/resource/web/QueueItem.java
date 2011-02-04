package com.digitallizard.bbcnewsreader.resource.web;

public class QueueItem {
	private String url;
	private int type;
	private int itemId;
	
	public QueueItem(String url, int type, int itemId, int priority)
	{
		this.url = url;
		this.type = type;
		this.itemId = itemId;
	}
	
	public int getType()
	{
		return type;
	}
	
	public String getUrl()
	{
		return url;
	}
	
	public int getItemId()
	{
		return itemId;
	}

}
