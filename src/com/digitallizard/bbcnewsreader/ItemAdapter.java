package com.digitallizard.bbcnewsreader;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class ItemAdapter extends ArrayAdapter<NewsItem> {
	private ArrayList<NewsItem> items;
	private int layout;
	private LayoutInflater inflater;
	private DatabaseHandler database;
	
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
		
		//try to load in an image
		byte[] imageBytes = database.getThumbnail(items.get(position).getId());
		//check if any image data was returned
		if(imageBytes != null){
			//try to construct an image out of the bytes given by the database
			Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length); //load the image into a bitmap
			ImageView imageView = (ImageView)view.findViewById(R.id.categoryItemImage);
			imageView.setImageBitmap(imageBitmap);
		}
		else{
			//set the image to the default image
			ImageView imageView = (ImageView)view.findViewById(R.id.categoryItemImage);
			imageView.setImageResource(R.drawable.no_thumb);
		}
		
		return view;
	}
	
	public void finish(){
		//shutdown the database
		database.finish();
	}
	
	public ItemAdapter(Context context, int layout, ArrayList<NewsItem> items){
		super(context, layout, items);
		
		this.items = items;
		this.layout = layout;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		database = new DatabaseHandler(context, 0); //don't bother with clear old date
	}
}
