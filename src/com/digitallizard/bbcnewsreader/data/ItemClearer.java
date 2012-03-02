package com.digitallizard.bbcnewsreader.data;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

public class ItemClearer implements Runnable {
	volatile boolean isClearing;
	Thread thread;
	ContentResolver contentResolver;
	long threshold;
	
	public void clearItems(ContentResolver contentResolver, long threshold) {
		// only allow clearing if the thread isn't running
		if (!isClearing) {
			this.isClearing = true;
			this.contentResolver = contentResolver;
			this.threshold = threshold;
			thread = new Thread(this);
			thread.start();
		}
	}
	
	public void run() {
		// FIXME Optimise, should use a join
		// find items older than the threshold
		Uri uri = DatabaseProvider.CONTENT_URI_ITEMS;
		String[] projection = { DatabaseHelper.COLUMN_ITEM_ID };
		String selection = DatabaseHelper.COLUMN_ITEM_PUBDATE + "<?";
		String[] selectionArgs = { Long.toString(threshold) };
		Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
		
		// find the column indexes
		int id = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID);
		
		// loop through and delete the items
		while (cursor.moveToNext()) {
			Uri tempUri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(cursor.getInt(id)));
			contentResolver.delete(tempUri, null, null);
		}
		
		cursor.close();
		
		// mark the clearing as finished
		isClearing = false;
	}
	
	public ItemClearer() {
		isClearing = false;
		thread = null;
		contentResolver = null;
		threshold = 0;
	}
}
