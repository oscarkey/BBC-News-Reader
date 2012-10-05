/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.resource.web;

import java.net.URL;
import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;

import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.ResourceInterface;

public class WebManager implements Runnable {
	/* constants */
	public static final int ITEM_TYPE_HTML = 2;
	public static final int ITEM_TYPE_THUMB = 1;
	public static final int ITEM_TYPE_IMAGE = 0;
	public static final int ERROR_FAIL_THRESHOLD = 4;
	
	/* variables */
	PriorityBlockingQueue<QueueItem> downloadQueue;
	ResourceInterface handler;
	private boolean keepDownloading;
	Thread downloadThread;
	private volatile boolean noError;
	private volatile int numErrors;
	
	public synchronized boolean isQueueEmpty() {
		return downloadQueue.isEmpty();
	}
	
	synchronized boolean shouldKeepDownloading() {
		return keepDownloading;
	}
	
	synchronized void setKeepDownloading(boolean keepDownloading) {
		this.keepDownloading = keepDownloading;
	}
	
	
	private void downloadItem(QueueItem item) {
		switch (item.getType()) {
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
	
	private void downloadHtml(QueueItem item) {
		try {
			byte[] html = HtmlParser.getPage(item.getUrl()); // load the page
			// before we report this download, check if it was a specific request
			if (item.wasSpecificallyRequested()) {
				handler.itemDownloadComplete(true, item.getItemId(), item.getType(), html);
			}
			else {
				handler.itemDownloadComplete(false, item.getItemId(), item.getType(), html);
			}
		} catch (Exception e) {
			numErrors++; // increment the number of errors
			if (numErrors > ERROR_FAIL_THRESHOLD) {
				// report the error
				// FIXME need to work out if internet error or not
				handler.reportError(ReaderActivity.ERROR_TYPE_INTERNET, "There was an error retrieving articles.", e.toString());
				stopDownload(); // give up loading
			}
		}
	}
	
	private void downloadThumbnail(QueueItem item) {
		try {
			URL url = new URL(item.getUrl());
			byte[] thumb = ImageDownloader.getImage(url); // load the image
			handler.itemDownloadComplete(false, item.getItemId(), item.getType(), thumb);
		} catch (Exception e) {
			numErrors++; // increment the number of errors
			if (numErrors > ERROR_FAIL_THRESHOLD) {
				// report the error
				// FIXME need to work out if internet error or not
				handler.reportError(ReaderActivity.ERROR_TYPE_INTERNET, "There was an error retrieving thumbnails.", e.toString());
				stopDownload(); // give up loading
			}
		}
	}
	
	private void downloadImage(QueueItem item) {
		try {
			URL url = new URL(item.getUrl());
			byte[] image = ImageDownloader.getImage(url); // load the image
			handler.itemDownloadComplete(false, item.getItemId(), item.getType(), image);
		} catch (Exception e) {
			numErrors++; // increment the number of errors
			if (numErrors > ERROR_FAIL_THRESHOLD) {
				// report the error
				handler.reportError(ReaderActivity.ERROR_TYPE_INTERNET, "There was an error retrieving images.", e.toString());
				e.printStackTrace();
			}
		}
	}
	
	private void itemQueued() {
		// check if we need to start the download thread
		if (!shouldKeepDownloading()) {
			// start the download thread
			setKeepDownloading(true);
			noError = true;
			downloadThread = new Thread(this);
			downloadThread.start();
		}
	}
	
	public void addToQueue(String url, int type, int itemId) {
		// just call the main function with the type as the priority
		addToQueue(url, type, itemId, type);
	}
	
	public void addToQueue(String url, int type, int itemId, int priority) {
		QueueItem queueItem = new QueueItem(url, type, itemId, priority);
		downloadQueue.add(queueItem);
		itemQueued();
	}
	
	public void loadNow(String url, int type, int itemId) {
		// check if a load is in progress
		if (shouldKeepDownloading()) {
			// loop through the queue to find the item we want, then boost its priority
			boolean itemExists = false; // set to true if the item was actually in the queue
			// FIXME looping efficient? probably doesn't matter as only on user command
			Iterator<QueueItem> iterator = downloadQueue.iterator();
			while (iterator.hasNext()) {
				// check the id of this item
				QueueItem item = iterator.next();
				if (item.getItemId() == itemId) {
					// boost the priority of this item
					item.setPriority(QueueItem.PRIORITY_DOWNLOAD_NOW);
					itemExists = true; // we found the item
				}
			}
			// if the item wasn't found, create it and set its priority high
			if (!itemExists) {
				addToQueue(url, type, itemId, QueueItem.PRIORITY_DOWNLOAD_NOW);
			}
		}
		else {
			// clear the queue, just in case
			emptyQueue();
			// add the item to the queue, this will automatically start the download
			addToQueue(url, type, itemId, QueueItem.PRIORITY_DOWNLOAD_NOW);
		}
	}
	
	public void emptyQueue() {
		// check if a download is in progress
		if (shouldKeepDownloading()) {
			stopDownload(); // first stop downloading
		}
		downloadQueue.clear(); // empty the queue
	}
	
	public void stopDownload() {
		// check if the download is going
		if (shouldKeepDownloading()) {
			// try and stop the download
			noError = false;
			setKeepDownloading(false); // this will stop it after the current file
			emptyQueue(); // empty the queue
		}
		else {
			// as a load isn't in progress we can report that we have finished
			handler.fullLoadComplete(false);
		}
	}
	
	public void run() {
		// check this hasn't been called in error
		if (!isQueueEmpty()) {
			// keep downloading if we should
			while (shouldKeepDownloading()) {
				// retrieve the head of the queue and load it
				downloadItem(downloadQueue.poll());
				// check if the queue is empty now
				if (isQueueEmpty()) {
					setKeepDownloading(false); // stop the loop
				}
			}
			handler.fullLoadComplete(noError); // report that the load is complete
		}
	}
	
	public WebManager(ResourceInterface handler) {
		this.handler = handler;
		downloadQueue = new PriorityBlockingQueue<QueueItem>();
		numErrors = 0; // no errors yet
	}
}
