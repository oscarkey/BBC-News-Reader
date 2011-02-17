package com.digitallizard.bbcnewsreader.resource.web;

public class QueueItem implements Comparable<QueueItem> {
	private String url;
	private int type;
	private int itemId;
	private int priority;
	
	public QueueItem(String url, int type, int itemId)
	{
		this.url = url;
		this.type = type;
		this.itemId = itemId;
		this.priority = type;
	}
	
	public int compareTo(QueueItem item){
		if(this.priority > item.priority)
			return 1;
		if(this.priority < item.priority)
			return -1;
		if(this.priority == item.priority)
			return 0;
		return 0; //FIXME this is unneeded
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
