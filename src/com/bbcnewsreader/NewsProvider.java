package com.bbcnewsreader;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class NewsProvider extends ContentProvider {
	
	/** static definitions */
	private static String PROVIDER_NAME = "com.bbcnewsreader.newsprovider"; //our name
	
	//define the content uris
	public static final Uri CONTENT_URI = Uri.parse("content://"+PROVIDER_NAME);
	public static final Uri CATEGORY_URI = Uri.parse("content://com.bbcnewsreader.newsprovider/categories");
	//public static final Uri NEWS_URI = Uri.parse("content://com.bbcnewsreader.newsprovider/newsitems");
	//TODO add support for news items
	
	
	
	//query type codes
	private static final int CATEGORIES = 0;
	private static final int CATEGORY_ID = 1;
	
	//match uris to the actual codes
	private static final UriMatcher uriMatcher;
	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PROVIDER_NAME, "categories", CATEGORIES);
		uriMatcher.addURI(PROVIDER_NAME, "categories/#", CATEGORY_ID);
	}
	
	private static final String DATABASE_NAME = "bbcnewsreader.db";
	private static final int DATABASE_VERSION = 1;
	private static final String NEWS_TABLE_NAME = "items";
	private static final String CATEGORY_TABLE_NAME = "categories";
	private static final String RELATIONSHIP_TABLE_NAME = "categories_items";
	
	/* variables */
	Context context;
	SQLiteDatabase database;
	
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
		default:
			throw new IllegalArgumentException("Unsupported URI: " + uri);
		}
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
		OpenHelper helper = new OpenHelper(context);
		database = helper.getWritableDatabase();
		if(database != null)
			return true;
		else
			return false;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder(); //this helps us make a query for the database
		if(uriMatcher.match(uri) == CATEGORIES){
			builder.setTables(CATEGORY_TABLE_NAME);
			//builder.appendWhere(inWhere)
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	/* the helper to assist with database creation */
	private static class OpenHelper extends SQLiteOpenHelper {

	    OpenHelper(Context context) {
	       super(context, DATABASE_NAME, null, DATABASE_VERSION);
	    }

	    @Override
	    public void onCreate(SQLiteDatabase db) {
	  	  //Item table
	       db.execSQL("CREATE TABLE " + NEWS_TABLE_NAME + 
	        "(item_Id integer PRIMARY KEY," +
	        "title varchar(255), " +
	        "description varchar(255), " +
	        "link varchar(255), " +
	        "pubdate varchar(255))");
	       //Category table
	       db.execSQL("CREATE TABLE " + CATEGORY_TABLE_NAME +
	        "(category_Id integer PRIMARY KEY," +
	        "name varchar(255)," +
	        "enabled boolean)");
	       //Link table
	       db.execSQL("CREATE TABLE " + RELATIONSHIP_TABLE_NAME +
	        "(categoryName varchar(255), " +
	        "itemId INT)");
	    }

	    @Override
	    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	       db.execSQL("DROP TABLE IF EXISTS " + NEWS_TABLE_NAME);
	       db.execSQL("DROP TABLE IF EXISTS " + CATEGORY_TABLE_NAME);
	       db.execSQL("DROP TABLE IF EXISTS " + RELATIONSHIP_TABLE_NAME);
	       onCreate(db);
	    }
	 }
}
