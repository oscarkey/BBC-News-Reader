/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.resource.web;

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
