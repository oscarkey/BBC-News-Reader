/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<Item> {
	private ArrayList<Item> items;
	private int layout;
	private LayoutInflater inflater;
	private int thumbWidth;
	private int thumbHeight;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			// initialise the view
			view = inflater.inflate(layout, null);
		}
		
		// set the values for this view
		TextView title = (TextView) view.findViewById(R.id.textNewsItemTitle);
		title.setText(items.get(position).getTitle());
		
		// try to load in an thumbnail
		byte[] thumbnailBytes = items.get(position).getThumbnailBytes();
		ImageView imageView = (ImageView) view.findViewById(R.id.imageNewsItem);
		
		// check if any data was returned
		if (Arrays.equals(thumbnailBytes, ReaderActivity.NO_THUMBNAIL_URL_CODE)) {
			// set the image to the loaded but no image thumnail
			imageView.setImageResource(R.drawable.no_thumb);
		}
		else if (thumbnailBytes != null) {
			// try to construct an image out of the bytes given by the database
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length); // load the image into a bitmap
			imageView.setImageBitmap(imageBitmap);
		}
		else {
			// set the image to the default thumbnail
			imageView.setImageResource(R.drawable.no_thumb_grey);
		}
		
		// set the image size
		ViewGroup.LayoutParams layout = imageView.getLayoutParams();
		layout.width = thumbWidth;
		layout.height = thumbHeight;
		imageView.setLayoutParams(layout);
		
		return view;
	}
	
	
	public void finish() {
		// do nothing
	}
	
	public ItemAdapter(Context context, int layout, ArrayList<Item> items, int thumbWidth, int thumbHeight) {
		super(context, layout, items);
		this.items = items;
		this.layout = layout;
		this.thumbWidth = thumbWidth;
		this.thumbHeight = thumbHeight;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
