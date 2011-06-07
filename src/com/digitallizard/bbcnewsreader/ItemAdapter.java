package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;
import java.util.Arrays;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ItemAdapter extends ArrayAdapter<NewsItem> {
	private ArrayList<NewsItem> items;
	private int layout;
	private LayoutInflater inflater;
	
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		if(view == null){
			//initialise the view
			view = inflater.inflate(layout, null);
		}
		
		//set the values for this view
		TextView title = (TextView)view.findViewById(R.id.categoryItemName);
		TextView desc = (TextView)view.findViewById(R.id.categoryItemDescription);
		title.setText(items.get(position).getTitle());
		desc.setText(items.get(position).getDescription());
		
		//try to load in an thumbnail
		byte[] thumbnailBytes = items.get(position).getThumbnailBytes();
		//check if any data was returned
		if(Arrays.equals(thumbnailBytes,ReaderActivity.NO_THUMBNAIL_URL_CODE)){
			//set the image to the loaded but no image thumnail
			ImageView imageView = (ImageView)view.findViewById(R.id.categoryItemImage);
			imageView.setImageResource(R.drawable.no_thumb);
			Log.v("ItemAdapter", "It ran!");
		}
		else if(thumbnailBytes != null){
			//try to construct an image out of the bytes given by the database
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(thumbnailBytes, 0, thumbnailBytes.length); //load the image into a bitmap
			ImageView imageView = (ImageView)view.findViewById(R.id.categoryItemImage);
			imageView.setImageBitmap(imageBitmap);
			Log.v("ItemAdapter", "thumbnailBytes");
		}
		else{
			//set the image to the default thumbnail
			ImageView imageView = (ImageView)view.findViewById(R.id.categoryItemImage);
			imageView.setImageResource(R.drawable.no_thumb_grey);
		}
		
		return view;
	}
	
	public void finish(){
		//do nothing
	}
	
	public ItemAdapter(Context context, int layout, ArrayList<NewsItem> items){
		super(context, layout, items);
		this.items = items;
		this.layout = layout;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
}
