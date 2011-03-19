package com.digitallizard.bbcnewsreader;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ItemLayout extends LinearLayout {
	private int id;
	private String url;
	private TextView title;
	private ImageView image;
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
	}
	
	public String getUrl(){
		return url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}
	
	public void setTitle(String text){
		//check if we have a pointer to the title
		if(title != null){
			title.setText(text); //set the text
		}
		else{
			//find the title then set it
			title = (TextView)this.findViewById(R.id.textNewsItemTitle);
			title.setText(text);
		}
	}
	
	public void setImage(Bitmap bitmap){
		if(image != null){
			image.setImageBitmap(bitmap);
		}
	}

	public ItemLayout(Context context) {
		super(context); //just call the super function
	}

	public ItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs); //just call the super function
	}
}
