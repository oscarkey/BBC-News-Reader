package com.bbcnewsreader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.database.sqlite.SQLiteStatement;

public class NewsProvider extends ContentProvider {
	
	/** static definitions */
	private static String PROVIDER_NAME = "com.bbcnewsreader.newsprovider"; //our name
	
	//define the content uris
	public static final Uri CONTENT_URI = Uri.parse("content://"+PROVIDER_NAME);
	public static final Uri CATEGORY_URI = Uri.parse("content://com.bbcnewsreader.newsprovider/categories");
	//public static final Uri NEWS_URI = Uri.parse("content://com.bbcnewsreader.newsprovider/newsitems");
	//TODO add support for news items
	
	private static final int CATEGORIES = 0;
	private static final int CATEGORY_ID = 1;
	
	//match uris to the actual codes
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "categories", CATEGORIES);
		uriMatcher.addURI(PROVIDER_NAME, "categories/#", CATEGORY_ID);
	}
	
	/** variables */
	Context context;
	//Database db;
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		//choose a specific data type to handle
		switch(uriMatcher.match(uri)){
		//all the categories
		case(CATEGORIES):
			return "android.database.Cursor";
		case(CATEGORY_ID):
			return "android.database.Cursor";
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		context = getContext(); //load in the context, whatever that is
		//open a connection to the database
		//SQLiteOpenHelper openHelper = new SQLiteOpenHelper(context, "ReaderDB",);
		return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
