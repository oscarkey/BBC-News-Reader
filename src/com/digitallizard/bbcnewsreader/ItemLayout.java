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
	
	public boolean isItem(){
		//if the id is not -1 then this item has been set
		if(id != -1){
			return true;
		}
		else{
			return false;
		}
	}
	
	public int getId(){
		return id;
	}
	
	public void setId(int id){
		this.id = id;
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
	
	public void setImage(int drawable){
		if(image != null){
			image.setImageResource(drawable);
		}
	}

	public ItemLayout(Context context) {
		super(context); //just call the super function
		id = -1;
	}

	public ItemLayout(Context context, AttributeSet attrs) {
		super(context, attrs); //just call the super function
		id = -1;
	}
}
