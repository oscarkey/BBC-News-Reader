package com.digitallizard.bbcnewsreader.resource.web;

import java.net.URI;
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
	}

	synchronized boolean shouldKeepDownloading() {
		return keepDownloading;
	}

	synchronized void setKeepDownloading(boolean keepDownloading) {
		this.keepDownloading = keepDownloading;
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
			handler.itemDownloadComplete(false, item.getItemId(), item.getType(), html);
		}
		catch(Exception e){
			handler.reportError(false, "There was an error retrieving the article.", e.getMessage());
		}
	}
	
	private void downloadThumbnail(QueueItem item){
		
	}
	
	private void downloadImage(QueueItem item){
		
	}
	
	private void itemQueued(){
		//check if we need to start the download thread
		if(isQueueEmpty() && !downloadThread.isAlive()){
			//start the download thread
			setQueueEmpty(false);
			setKeepDownloading(true);
			downloadThread.start();
		}
	}
	
	public void addToQueue(String url, int type, int itemId){
		QueueItem queueItem = new QueueItem(url, type, itemId);
		getQueue().add(queueItem);
		itemQueued();
	}
	
	public void loadNow(String url, int type, int itemId){
		//TODO boost the priority of the item to load in now
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
	}
	
	public WebManager(ResourceInterface handler){
		this.handler = handler;
		setQueueEmpty(true);
		downloadQueue = new PriorityQueue<QueueItem>();
		downloadThread = new Thread(this);
	}
}