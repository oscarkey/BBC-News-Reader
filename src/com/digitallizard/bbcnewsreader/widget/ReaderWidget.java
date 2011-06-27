package com.digitallizard.bbcnewsreader.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

import com.digitallizard.bbcnewsreader.NewsItem;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class ReaderWidget extends AppWidgetProvider {
	private static final int NUM_ITEMS = 5; // the number of items to flip through
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		// retrieve the news from the database
		DatabaseHandler database = new DatabaseHandler(context);
		NewsItem[] items = database.getItems("Headlines", NUM_ITEMS);
		
		// create references to the required view
		RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
		
		// remote existing views from the flipper
		view.removeAllViews(R.id.widgetFlipper);
		
		// loop through and add the latest news to the item
		for(int i = 0; i < NUM_ITEMS && i < items.length; i++){
			// create a view for this item
			RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.widget_item);
			// set the text
			item.setTextViewText(R.id.widgetItemTitle, items[i].getTitle());
			item.setTextViewText(R.id.widgetItemDesc, items[i].getDescription());
			
			// add this item to the flipper
			view.addView(R.id.widgetFlipper, item);
		}
		
		// update the widget with the updated views
		ComponentName widget = new ComponentName(context, ReaderWidget.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(widget, view);
	}
	
}
