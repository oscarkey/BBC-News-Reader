package com.digitallizard.bbcnewsreader;

public class NewsItem {
	private String title;
	private String description;
	private String link;
	private int id;
	private byte[] thumbnailBytes;
	
	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}
	/**
	 * @param title the title to set
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
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}
	/**
	 * @param link the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
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
	 * @param thumbnailBytes the thumbnailBytes to set
	 */
	public void setThumbnailBytes(byte[] imageBytes) {
		this.thumbnailBytes = imageBytes;
	}
	
	public NewsItem(int id, String title, String description, String link, byte[] imageBytes) {
		super();
		this.title = title;
		this.description = description;
		this.link = link;
		this.id = id;
		this.thumbnailBytes = imageBytes;
	}
}
