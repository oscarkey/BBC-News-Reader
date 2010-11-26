package com.bbcnewsreader.resource.web;

import java.util.Queue;

public class WebManager {
	/* constants */
	
	
	/* variables */
	boolean extractData; //extract or just save the entire page
	boolean extractImage; //extract the image as well as the text
	Queue pageQueue;
	
	public void add(String url){
		//launch a new ContentExtracter to extract the text from the document
	}
	
	public WebManager(boolean extractData, boolean extractImage){
		this.extractData = extractData;
		this.extractImage = extractImage;
	}
}
