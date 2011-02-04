package com.bbcnewsreader.resource.web;

import java.net.URI;
import java.util.PriorityQueue;

import com.bbcnewsreader.ResourceInterface;

import android.util.Log;

public class WebManager {
	/* constants */
	static final int ITEM_TYPE_HTML = 0;
	static final int ITEM_TYPE_THUMB = 1;
	static final int ITEM_TYPE_IMAGE = 2;
	
	/* variables */
	PriorityQueue<QueueItem> downloadQueue;
	ResourceInterface resourceInterface;
	private boolean queueEmpty;
	
	private void itemQueued(){
		if(!queueEmpty){
			queueEmpty = false;
			startDownload();
		}
	}
	
	private void startDownload(){
		//FIXME Types need to change to constants
		QueueItem queueItem;
		while(downloadQueue.size() != 0){
			queueItem = downloadQueue.poll();
			switch(queueItem.getType()){
				case ITEM_TYPE_HTML:
					downloadHtml(queueItem);
					break;
				case ITEM_TYPE_THUMB:
					break;
				case ITEM_TYPE_IMAGE:
					downloadImage(queueItem);
					break;
			}
		}
	}
	
	void downloadHtml(QueueItem item){
		try{
			URI url = new URI(item.getUrl());
			String html = null;
			//html = HtmlParser.getPage(url);
			resourceInterface.downloadComplete(item.getItemId(), item.getType(), html);
		}
		catch(Exception e){
			Log.e("htmlparser",e.getMessage());
		}
	}
	
	void downloadThumbnail(QueueItem item){
		
	}
	
	void downloadImage(QueueItem item){
		
	}
	
	public void addToQueue(String url,int type,int itemId){
		QueueItem queueItem = new QueueItem(url, type, itemId, -1);
		downloadQueue.add(queueItem);
		itemQueued();
	}
	
	public WebManager(){
		downloadQueue = new PriorityQueue<QueueItem>();
	}
}
