package com.digitallizard.bbcnewsreader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class DatabaseProvider extends ContentProvider {
	
	/** constants **/
	public static final Uri CONTENT_URI = Uri.parse("content://com.digitallizard.bbcnewsreader");
	public static final Uri CONTENT_URI_CATEGORIES = Uri.parse("content://com.digitallizard.bbcnewsreader/categories");
	public static final Uri CONTENT_URI_ITEMS = Uri.parse("content://com.digitallizard.bbcnewsreader/items");
	
	public static final String COLUMN_CATEGORY_ID = "category_Id";
	public static final String COLUMN_CATEGORY_NAME = "name";
	public static final String COLUMN_CATEGORY_ENABLED = "enabled";
	public static final String COLUMN_CATEGORY_URL = "url";
	
	public static final String COLUMN_ITEM_ID = "item_Id";
	public static final String COLUMN_ITEM_TITLE = "title";
	public static final String COLUMN_ITEM_DESCRIPTION = "description";
	public static final String COLUMN_ITEM_PUBDATE = "pubdate";
	public static final String COLUMN_ITEM_URL = "link";
	public static final String COLUMN_ITEM_THUMBNAIL_URL = "thumbnailurl";
	public static final String COLUMN_ITEM_HTML = "html";
	public static final String COLUMN_ITEM_THUMBNAIL = "thumbnail";
	
	
	/** variables **/
	DatabaseHelper database;

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri arg0, ContentValues arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selecton, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri arg0, ContentValues arg1, String arg2, String[] arg3) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean onCreate() {
		//initialise the database
		database = new DatabaseHelper(this.getContext());
		
		return false;
	}
	
	public DatabaseProvider() {
		// TODO Auto-generated constructor stub
	}

}
