/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemLayout extends LinearLayout {
	private int id;
	private TextView title;
	private ImageView image;
	private boolean imageLoaded;
	
	public boolean isItem() {
		// if the id is not -1 then this item has been set
		if (id != -1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public int getId() {
		return id;
	}
	
	@Override
	public void setId(int id) {
		this.id = id;
	}
	
	public void setTitle(String text) {
		// check if we have a pointer to the title
		if (title != null) {
			title.setText(text); // set the text
		}
		else {
			// find the title then set it
			title = (TextView) this.findViewById(R.id.textNewsItemTitle);
			title.setText(text);
		}
	}
	
	void initImage() {
		// create a reference to the image view
		image = (ImageView) this.findViewById(R.id.imageNewsItem);
	}
	
	public void setImage(Bitmap bitmap) {
		if (image == null) {
			initImage();
		}
		if (image != null) {
			image.setImageBitmap(bitmap);
		}
	}
	
	public void setImage(int drawable) {
		if (image == null) {
			initImage();
		}
		if (image != null) {
			image.setImageResource(drawable);
		}
	}
	
	public void setImageSize(int width, int height) {
		if (image == null) {
			initImage();
		}
		if (image != null) {
			image.setLayoutParams(new LayoutParams(width, height));
		}
	}
	
	public void setImageLoaded(boolean loaded) {
		imageLoaded = loaded;
	}
	
	public boolean isImageLoaded() {
		return imageLoaded;
	}
	
	public ItemLayout(Context context) {
		super(context); // just call the super function
		id = -1;
	}
	
	public ItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs); // just call the super function
		id = -1;
	}
}
