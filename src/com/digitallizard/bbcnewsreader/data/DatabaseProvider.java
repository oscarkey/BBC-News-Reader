package com.digitallizard.bbcnewsreader.data;

import java.util.Date;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DatabaseProvider extends ContentProvider {
	
	/** constants **/
	public static final String AUTHORITY = "com.digitallizard.bbcnewsreader";
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.digitallizard.bbcnewsreader");
	public static final Uri CONTENT_URI_CATEGORIES = Uri.parse("content://com.digitallizard.bbcnewsreader/categories");
	public static final Uri CONTENT_URI_ENABLED_CATEGORIES = Uri.withAppendedPath(CONTENT_URI_CATEGORIES, "enabled");
	public static final Uri CONTENT_URI_ITEMS = Uri.parse("content://com.digitallizard.bbcnewsreader/items");
	public static final Uri CONTENT_URI_ITEMS_BY_CATEGORY = Uri.withAppendedPath(CONTENT_URI_ITEMS, "category");
	public static final Uri CONTENT_URI_UNDOWNLOADED_ITEMS = Uri.withAppendedPath(CONTENT_URI_ITEMS, "undownloaded");
	
	//uri matcher helpers
	private static final int CATEGORIES = 1;
	private static final int ENABLED_CATEGORIES = 2;
	private static final int ITEMS = 4;
	private static final int ITEM_BY_ID = 5;
	private static final int ITEMS_BY_CATEGORY = 3;
	private static final int UNDOWNLOADED_ITEMS = 6;
	
	//uri matcher
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMatcher.addURI(AUTHORITY, "categories", CATEGORIES);
		uriMatcher.addURI(AUTHORITY, "categories/enabled", ENABLED_CATEGORIES);
		uriMatcher.addURI(AUTHORITY, "items/", ITEMS);
		uriMatcher.addURI(AUTHORITY, "items/#", ITEM_BY_ID);
		uriMatcher.addURI(AUTHORITY, "items/category/*", ITEMS_BY_CATEGORY);
		uriMatcher.addURI(AUTHORITY, "items/undownloaded/*", UNDOWNLOADED_ITEMS);
	}
	
	
	/** variables **/
	DatabaseHelper database;
	boolean methodInsertWithConflictExists;
	
	//compatibility checker
	private void checkCompatibility(){
		//check if the insertWithOnConflict exists
		try {
		    SQLiteDatabase.class.getMethod("insertWithOnConflict", new Class[] {String.class, String.class, ContentValues.class, Integer.TYPE});
		    //success, this method exists, set the boolean
		    methodInsertWithConflictExists = true;
		} catch (NoSuchMethodException e) {
		    //failure, set the boolean
		    methodInsertWithConflictExists = false;
		}
	}
	
	//get functions
	private Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return database.query(DatabaseHelper.CATEGORY_TABLE, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getEnabledCategories(String[] projection, String sortOrder) {
		//define a selection to only retrieve enabled categories
		String selection = "enabled='1'";
		//ask for categories by this selection
		return getCategories(projection, selection, null, sortOrder);
	}
	
	private Cursor getItems(String[] projection, String selection, String[] selectionArgs, String sortOrder){
		//get items
		return database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getItem(String[] projection, int id) {
		String selection = DatabaseHelper.COLUMN_ITEM_ID;
		String[] selectionArgs = new String[] {Integer.toString(id)};
		return database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, null);
	}
	
	private Cursor getItems(String[] projection, String category, String sortOrder){
		//get items in this category
		return null;
	}
	
	//insert functions
	private void insertItem(ContentValues values){
		//query to see if this item is already in the database
		String[] projection = new String[] {DatabaseHelper.COLUMN_ITEM_ID};
		String selection = DatabaseHelper.COLUMN_ITEM_URL + "=?";
		String[] selectionArgs = new String[] {values.getAsString(DatabaseHelper.COLUMN_ITEM_URL)};
		Cursor cursor = database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, null);
		
		//check if this item is not in the database
	    long id = -1; //holds the id of the item
	    if(cursor.getCount() == 0){
		    //insert the item
		    id = database.insert(DatabaseHelper.ITEM_TABLE, values); //this outputs the new primary key
	    }
	    else if(cursor.getCount() == 1){
		    //this item must already exist
		    cursor.moveToNext();
		    id = (long)cursor.getInt(0); //save the id
		    //test to see if the title has changed
		    if(!cursor.getString(1).equals(title)){
			    //update the title and clear the html and thumbnail
			    ContentValues values = new ContentValues(3);
			    values.put("title", title);
			    values.putNull("html");
			    values.putNull("thumbnail");
			    db.update(ITEM_TABLE, values, "item_Id=?", new String[] {Long.toString(id)});
		    }
	    }
	    //close the cursor
	    cursor.close();
	    
	    //associate the item with its category
	    ContentValues values = new ContentValues(3);
	    values.put("categoryName", category);
	    values.put("itemId", id);
	    values.put("priority", priority);
	    
	    //insert this item, if the required method doesn't exist, use the old one
	    if(methodInsertWithConflictExists){
		    //FIXME performance: shouldn't replace every time
		    WrapBackwards.insertWithOnConflict(db, ITEM_CATEGORY_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
	    }
	    else{
		    //use an alternative method
		    try{
			    database.insertOrThrow(ITEM_CATEGORY_TABLE, null, values);
		    } catch(SQLiteConstraintException e){
			    //this item obviously already exists, replace it instead
			    database.replace(ITEM_CATEGORY_TABLE, null, values);
		    } catch(SQLException e){
			    //TODO handle this type of exception
		    }
	   }
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		//try and match the uri
		switch(uriMatcher.match(uri)){
		case ITEMS:
			//insert the provided item
			insertItem(values);
			break;
		}
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		//try and match the queried uri
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
		case ITEMS:
			//query the database for items
			return this.getItems(projection, selection, selectionArgs, sortOrder);
		case ITEM_BY_ID:
			//query the database for this specific item
			int id = Integer.parseInt(uri.getLastPathSegment());
			return getItem(projection, id);
		case ITEMS_BY_CATEGORY:
			//query the database for items in this category
			String category = uri.getLastPathSegment();
			return getItems(projection, category, sortOrder);
		case UNDOWNLOADED_ITEMS:
			//query the database for undownloaded items of this type
			return null;
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri.toString());
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public boolean onCreate() {
		//initialise the database
		database = new DatabaseHelper(this.getContext());
		//check compatibility
		checkCompatibility();
		
		return true;
	}
	
	public DatabaseProvider() {
		// TODO Auto-generated constructor stub
	}

}
