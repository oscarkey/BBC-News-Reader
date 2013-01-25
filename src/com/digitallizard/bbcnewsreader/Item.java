/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

public class Item {
	private String title;
	private String description;
	private String url;
	private int id;
	private byte[] thumbnailBytes;
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}
	
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	
	/**
	 * @return the thumbnailBytes
	 */
	public byte[] getThumbnailBytes() {
		return thumbnailBytes;
	}
	
	/**
	 * @param thumbnailBytes
	 *            the thumbnailBytes to set
	 */
	public void setThumbnailBytes(byte[] thumbnailBytes) {
		this.thumbnailBytes = thumbnailBytes;
	}
	
	public Item(int id, String title, String description, String link, byte[] imageBytes) {
		this.title = title;
		this.description = description;
		this.url = link;
		this.id = id;
		this.thumbnailBytes = imageBytes;
	}
	
	public Item() {
	}
}
