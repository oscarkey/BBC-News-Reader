/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.digitallizard.bbcnewsreader.ArticleActivity;
import com.digitallizard.bbcnewsreader.Item;
import com.digitallizard.bbcnewsreader.R;
import com.digitallizard.bbcnewsreader.ReaderActivity;
import com.digitallizard.bbcnewsreader.data.DatabaseHandler;
import com.digitallizard.bbcnewsreader.data.DatabaseProvider;

public class ReaderWidget extends AppWidgetProvider {
	private static final int NUM_ITEMS = 5; // the number of items to flip through
	public static final String PREF_KEY_CATEGORY = "widget_category_"; // key for the category
	public static final String DEFAULT_CATEGORY = "Headlines"; // the default category
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// retrieve the news from the database
		DatabaseHandler database = new DatabaseHandler(context);
		SharedPreferences settings = context.getSharedPreferences(ReaderActivity.PREFS_FILE_NAME, Context.MODE_PRIVATE);
		
		for (int i = 0; i < appWidgetIds.length; i++) {
			String category = settings.getString(PREF_KEY_CATEGORY + appWidgetIds[i], DEFAULT_CATEGORY);
			Item[] items = database.getItems(category, NUM_ITEMS);
			
			// create references to the required view
			RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.widget);
			
			// make the bbc news logo clickable
			Intent appIntent = new Intent(context, ReaderActivity.class);
			appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			appIntent.setAction(Intent.ACTION_MAIN);
			PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			view.setOnClickPendingIntent(R.id.widgetLogo, appPendingIntent);
			
			// remote existing views from the flipper
			view.removeAllViews(R.id.widgetFlipper);
			
			// loop through and add the latest news to the item
			for (int j = 0; j < NUM_ITEMS && j < items.length; j++) {
				// create a view for this item
				RemoteViews item = new RemoteViews(context.getPackageName(), R.layout.widget_item);
				// set the text
				item.setTextViewText(R.id.widgetItemTitle, items[j].getTitle());
				item.setTextViewText(R.id.widgetItemDesc, items[j].getDescription());
				
				// make the item clickable
				Intent itemIntent = new Intent(context, ArticleActivity.class);
				itemIntent.setData(Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(items[j].getId())));
				itemIntent.putExtra(ArticleActivity.EXTRA_KEY_ITEM_ID, items[j].getId());
				itemIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				itemIntent.setAction(Intent.ACTION_MAIN);
				PendingIntent itemPendingIntent = PendingIntent.getActivity(context, j, itemIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				item.setOnClickPendingIntent(R.id.widgetItemTitle, itemPendingIntent);
				item.setOnClickPendingIntent(R.id.widgetItemDesc, itemPendingIntent);
				
				// add this item to the flipper
				view.addView(R.id.widgetFlipper, item);
			}
			
			// update the widget with the updated views
			AppWidgetManager manager = AppWidgetManager.getInstance(context);
			manager.updateAppWidget(appWidgetIds[i], view);
		}
	}
	
}
