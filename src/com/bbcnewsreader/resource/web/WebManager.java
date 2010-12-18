package com.bbcnewsreader.resource.web;

public class WebManager {
	/* constants */
	
	
	/* variables */
	boolean extractData; //extract or just save the entire page
	boolean extractImage; //extract the image as well as the text
	
	public void loadFromDesktopUrl(String url){
		//launch a new ContentExtracter to extract the text from the document
	}
	
	public WebManager(boolean extractData, boolean extractImage){
		this.extractData = extractData;
		this.extractImage = extractImage;
	}
}
