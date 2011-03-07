package com.digitallizard.bbcnewsreader.resource.web;

public class QueueItem implements Comparable<QueueItem> {
	public static final int PRIORITY_DOWNLOAD_NOW = 5; //the priority if instant download is needed
	private String url;
	private int type;
	private int itemId;
	private int priority;
	
	public QueueItem(String url, int type, int itemId, int priority)
	{
		this.url = url;
		this.type = type;
		this.itemId = itemId;
		this.priority = priority;
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
	
	public boolean wasSpecificallyRequested(){
		if(priority == PRIORITY_DOWNLOAD_NOW){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void setPriority(int priority){
		this.priority = priority;
	}
}
