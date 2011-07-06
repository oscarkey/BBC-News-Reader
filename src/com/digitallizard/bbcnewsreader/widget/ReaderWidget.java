package com.digitallizard.bbcnewsreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.digitallizard.bbcnewsreader.ArticleActivity;
import com.digitallizard.bbcnewsreader.NewsItem;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;

public class ReaderWidget extends AppWidgetProvider {
	private static final int NUM_ITEMS = 5; // the number of items to flip through
	public static final String PREF_KEY_CATEGORY = "widget_category_"; // key for the category
	public static final String DEFAULT_CATEGORY = "Headlines"; // the default category
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName widget = new ComponentName(context, ReaderWidget.class);
        for(int i = 0; i < appWidgetIds.length; i++){
        	Log.e("widget", "widget: "+manager.getAppWidgetInfo(appWidgetIds[i]).toString());
        }
        Log.e("widget", "id length: "+appWidgetManager.getAppWidgetIds(widget).length);
        
		// retrieve the news from the database
		DatabaseHandler database = new DatabaseHandler(context);
		SharedPreferences settings = context.getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, Context.MODE_PRIVATE);
		
		for(int i = 0; i < appWidgetIds.length; i++){
			String category = settings.getString(PREF_KEY_CATEGORY + appWidgetIds[i], DEFAULT_CATEGORY);
			NewsItem[] items = database.getItems(category, NUM_ITEMS);
			
			Log.e("widget", "got "+category+" from "+PREF_KEY_CATEGORY + appWidgetIds[i]);
			
			// create references to the required view
			RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
			
			// make the bbc news logo clickable
			Intent appIntent = new Intent(context, ReaderActivity.class);
			PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			view.setOnClickPendingIntent(R.id.widgetLogo, appPendingIntent);
			
			// remote existing views from the flipper
			view.removeAllViews(R.id.widgetFlipper);
			
			// loop through and add the latest news to the item
			for(int j = 0; j < NUM_ITEMS && j < items.length; j++){
				// create a view for this item
				RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.widget_item);
				// set the text
				item.setTextViewText(R.id.widgetItemTitle, items[j].getTitle());
				item.setTextViewText(R.id.widgetItemDesc, items[j].getDescription());
				
				// make the item clickable
				Intent itemIntent = new Intent(context, ArticleActivity.class);
				itemIntent.putExtra("id", items[j].getId());
				PendingIntent itemPendingIntent = PendingIntent.getActivity(context, j, itemIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
				item.setOnClickPendingIntent(R.id.widgetItemTitle, itemPendingIntent);
				item.setOnClickPendingIntent(R.id.widgetItemDesc, itemPendingIntent);
				
				// add this item to the flipper
				view.addView(R.id.widgetFlipper, item);
			}
			
			// update the widget with the updated views
			//ComponentName widget = new ComponentName(context, ReaderWidget.class);
	        manager.updateAppWidget(appWidgetIds[i], view);
	        //manager.updateAppWidget(widget, view);
		}
	}
	
}
