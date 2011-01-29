package com.bbcnewsreader.resource.web;

import java.net.URI;
import java.util.PriorityQueue;

import com.bbcnewsreader.ResourceInterface;

import android.util.Log;

public class WebManager {
	/* constants */
	private boolean queueEmpty;
	
	/* variables */
	PriorityQueue<QueueItem> downloadQueue;
	ResourceInterface resourceInterface;
	
	public void addToQueue(String url,int type,int itemId){
		QueueItem queueItem=new QueueItem(url, type, itemId, -1);
		downloadQueue.add(queueItem);
		itemQueued();
	}
	private void itemQueued()
	{
		if(!queueEmpty)
		{
			queueEmpty=false;
			startDownload();
		}
	}
	private void startDownload()
	{
		//FIXME Types need to change to constants
		QueueItem queueItem;
		while(downloadQueue.size()!=0)
		{
			queueItem=downloadQueue.poll();
			switch(queueItem.getType())
			{
				case 0:downloadHtml(queueItem);break;
				case 1:
				case 2:downloadImage(queueItem);
			}
		}
	}
	
	public void downloadHtml(QueueItem item)
	{
		try
		{
			URI url=new URI(item.getUrl());
			String html=null;
			//html=HtmlParser.getPage(url);
			resourceInterface.downloadComplete(item.getItemId(), item.getType(), html);
		}
		catch(Exception e)
		{
			Log.e("htmlparser",e.getMessage());
		}
	}
	
	public void downloadImage(QueueItem item)
	{
		
	}
	
	public WebManager(){
		downloadQueue=new PriorityQueue<QueueItem>();
	}
}
