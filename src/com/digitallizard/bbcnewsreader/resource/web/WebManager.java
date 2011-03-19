package com.digitallizard.bbcnewsreader.resource.web;

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.PriorityQueue;

import com.digitallizard.bbcnewsreader.ResourceInterface;

import android.util.Log;

public class WebManager implements Runnable {
	/* constants */
	public static final int ITEM_TYPE_HTML = 2;
	public static final int ITEM_TYPE_THUMB = 1;
	public static final int ITEM_TYPE_IMAGE = 0;
	
	/* variables */
	PriorityQueue<QueueItem> downloadQueue;
	ResourceInterface handler;
	private boolean queueEmpty;
	private boolean keepDownloading;
	Thread downloadThread;
	

	synchronized boolean isQueueEmpty() {
		return queueEmpty;
	}

	synchronized void setQueueEmpty(boolean queueEmpty) {
		this.queueEmpty = queueEmpty;
		Log.v("web manager", "queueEmpty set to: "+queueEmpty);
	}

	synchronized boolean shouldKeepDownloading() {
		return keepDownloading;
	}

	synchronized void setKeepDownloading(boolean keepDownloading) {
		this.keepDownloading = keepDownloading;
		Log.v("web manager", "keepDownloading set to: "+keepDownloading);
	}
	
	synchronized void setQueue(PriorityQueue<QueueItem> queue){
		downloadQueue = queue;
	}
	
	synchronized PriorityQueue<QueueItem> getQueue(){
		return downloadQueue;
	}
	
	private void downloadItem(QueueItem item){
		switch(item.getType()){
			case ITEM_TYPE_HTML:
				downloadHtml(item);
				break;
			case ITEM_TYPE_THUMB:
				downloadThumbnail(item);
				break;
			case ITEM_TYPE_IMAGE:
				downloadImage(item);
				break;
		}
	}
	
	private void downloadHtml(QueueItem item){
		try{
			URI url = new URI(item.getUrl());
			String html = HtmlParser.getPage(url); //load the page
			//before we report this download, check if it was a specific request
			if(item.wasSpecificallyRequested())
				handler.itemDownloadComplete(true, item.getItemId(), item.getType(), html);
			else
				handler.itemDownloadComplete(false, item.getItemId(), item.getType(), html);
		}
		catch(Exception e){
			handler.reportError(false, "There was an error retrieving the article.", e.toString());
			e.printStackTrace();
		}
	}
	
	private void downloadThumbnail(QueueItem item){
		try{
			URL url = new URL(item.getUrl());
			byte[] thumb = ImageDownloader.getImage(url); //load the image
			handler.itemDownloadComplete(false, item.getItemId(), item.getType(), thumb);
		}
		catch(Exception e){
			handler.reportError(false, "There was an error retrieving the thumbnail.", e.toString());
			e.printStackTrace();
		}
	}
	
	private void downloadImage(QueueItem item){
		try{
			URL url = new URL(item.getUrl());
			byte[] image = ImageDownloader.getImage(url); //load the image
			handler.itemDownloadComplete(false, item.getItemId(), item.getType(), image);
		}
		catch(Exception e){
			handler.reportError(false, "There was an error retrieving the image.", e.toString());
			e.printStackTrace();
		}
	}
	
	private void itemQueued(){
		//check if we need to start the download thread
		if(isQueueEmpty() && !shouldKeepDownloading()){
			//start the download thread
			setQueueEmpty(false);
			setKeepDownloading(true);
			downloadThread = new Thread(this);
			downloadThread.start();
		}
	}
	
	public void addToQueue(String url, int type, int itemId){
		//just call the main function with the type as the priority
		addToQueue(url, type, itemId, type);
	}
	
	public void addToQueue(String url, int type, int itemId, int priority){
		QueueItem queueItem = new QueueItem(url, type, itemId, priority);
		getQueue().add(queueItem);
		itemQueued();
	}
	
	public void loadNow(String url, int type, int itemId){
		Log.v("web manager", "loading now:");
		boolean itemExists = false; //set to true if the item was actually in the queue
		//loop through the queue to find the item we want
		//FIXME looping efficient? probably doesn't matter as only on user command
		Iterator<QueueItem> iterator = getQueue().iterator();
		while(iterator.hasNext()){
			//check the id of this item
			QueueItem item = iterator.next();
			if(item.getItemId() == itemId){
				//boost the priority of this item
				item.setPriority(QueueItem.PRIORITY_DOWNLOAD_NOW);
				itemExists = true; //we found the item
			}
		}
		//if the item wasn't found, create it and set its priority high
		if(!itemExists){
			Log.v("web manager", "adding to queue");
			addToQueue(url, type, itemId, QueueItem.PRIORITY_DOWNLOAD_NOW);
		}
	}
	
	public void emptyQueue(){
		stopDownload(); //first stop downloading
		getQueue().clear(); //empty the queue
	}
	
	public void stopDownload(){
		//check if the download is going
		if(shouldKeepDownloading()){
			//try and stop the download
			setKeepDownloading(false); //this will stop it after the current file
		}
		else{
			//as a load isn't in progress we can report that we have finished
			handler.fullLoadComplete();
		}
	}
	
	public void run(){
		Log.v("webmanager", "thread starting");
		//check this hasn't been called in error
		if(getQueue().size() > 0){
			//keep downloading if we should
			while(shouldKeepDownloading()){
				//retrieve the head of the queue and load it
				downloadItem(getQueue().poll());
				//check if the queue is empty now
				if(getQueue().size() == 0){
					setQueueEmpty(true); //flag the queue as empty
					setKeepDownloading(false); //stop the loop
				}
			}
			handler.fullLoadComplete(); //report that the load is complete
		}
		else{
			//as the queue was empty, we should flag it
			setQueueEmpty(true);
		}
		Log.v("webmanager", "thread finishing");
	}
	
	public WebManager(ResourceInterface handler){
		this.handler = handler;
		setQueueEmpty(true);
		downloadQueue = new PriorityQueue<QueueItem>();
	}
}