package com.digitallizard.bbcnewsreader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class DatabaseProvider extends ContentProvider {
	
	/** constants **/
	public static final String AUTHORITY = "com.digitallizard.bbcnewsreader";
	public static final Uri CONTENT_URI = Uri.parse("content://com.digitallizard.bbcnewsreader");
	public static final Uri CONTENT_URI_CATEGORIES = Uri.parse("content://com.digitallizard.bbcnewsreader/categories");
	public static final Uri CONTENT_URI_ITEMS = Uri.parse("content://com.digitallizard.bbcnewsreader/items");
	
	//uri matcher helpers
	private static final int CATEGORIES = 1;
	private static final int ENABLED_CATEGORIES = 2;
	
	//uri matcher
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMatcher.addURI(AUTHORITY, "categories", CATEGORIES);
		uriMatcher.addURI(AUTHORITY, "categories/enabled", ENABLED_CATEGORIES);
	}
	
	
	/** variables **/
	DatabaseHelper database;
	
	
	private Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return database.query(DatabaseHelper.CATEGORY_TABLE, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getEnabledCategories(String[] projection, String sortOrder) {
		//define a selection to only retrieve enabled categories
		String selection = "enabled='1'";
		//ask for categories by this selection
		return getCategories(projection, selection, null, sortOrder);
	}
	
	
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
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//try and match the queried url
		switch(uriMatcher.match(uri)){
		case CATEGORIES:
			//query the database for all the categories
			if(selection == null){
				throw new IllegalArgumentException("Uri requires selection: " + uri.toString());
			}
			return getCategories(projection, selection, selectionArgs, sortOrder);
		case ENABLED_CATEGORIES:
			//query the database for enabled categories
			if(selection == null){
				throw new IllegalArgumentException("Uri requires selection: " + uri.toString());
			}
			return getEnabledCategories(projection, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri.toString());
		}
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
