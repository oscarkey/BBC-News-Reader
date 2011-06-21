package com.digitallizard.bbcnewsreader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
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
	
	// uri matcher helpers
	private static final int CATEGORIES = 1;
	private static final int ENABLED_CATEGORIES = 2;
	private static final int ITEMS = 4;
	private static final int ITEM_BY_ID = 5;
	private static final int ITEMS_BY_CATEGORY = 3;
	private static final int UNDOWNLOADED_ITEMS = 6;
	
	// uri matcher
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
	
	// compatibility checker
	private void checkCompatibility() {
		// check if the insertWithOnConflict exists
		try {
			SQLiteDatabase.class.getMethod("insertWithOnConflict", new Class[] { String.class, String.class, ContentValues.class, Integer.TYPE });
			// success, this method exists, set the boolean
			methodInsertWithConflictExists = true;
		} catch (NoSuchMethodException e) {
			// failure, set the boolean
			methodInsertWithConflictExists = false;
		}
	}
	
	// get functions
	private Cursor getCategories(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		return database.query(DatabaseHelper.CATEGORY_TABLE, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getEnabledCategories(String[] projection, String sortOrder) {
		// define a selection to only retrieve enabled categories
		String selection = "enabled='1'";
		// ask for categories by this selection
		return getCategories(projection, selection, null, sortOrder);
	}
	
	private Cursor getItems(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// get items
		return database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getItem(String[] projection, int id) {
		String selection = DatabaseHelper.COLUMN_ITEM_ID;
		String[] selectionArgs = new String[] { Integer.toString(id) };
		return database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, null);
	}
	
	private Cursor getItems(String[] projection, String category, String sortOrder) {
		// get items in this category
		String table = DatabaseHelper.ITEM_TABLE + "JOIN" + DatabaseHelper.RELATIONSHIP_TABLE + "ON" + DatabaseHelper.ITEM_TABLE + "." + 
			DatabaseHelper.COLUMN_ITEM_ID + "=" + DatabaseHelper.RELATIONSHIP_TABLE + "." + DatabaseHelper.COLUMN_RELATIONSHIP_ITEM_ID;
		String selection = DatabaseHelper.RELATIONSHIP_TABLE + "." + DatabaseHelper.COLUMN_RELATIONSHIP_CATEGORY_NAME + "=";
		String[] selectionArgs = new String[] {category};
		return database.query(table, projection, selection, selectionArgs, sortOrder);
	}
	
	private Cursor getUndownloadedItems(String[] projection, int numItems){
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setDistinct(true);
		queryBuilder.setTables(DatabaseHelper.ITEM_TABLE + " JOIN " + DatabaseHelper.RELATIONSHIP_TABLE + " ON " + DatabaseHelper.ITEM_TABLE + 
				"." + DatabaseHelper.COLUMN_ITEM_ID + "=" + DatabaseHelper.RELATIONSHIP_TABLE + "." + DatabaseHelper.COLUMN_RELATIONSHIP_ITEM_ID);
		queryBuilder.setTables("items JOIN categories_items ON items.item_Id=categories_items.itemId");
		String selection = DatabaseHelper.RELATIONSHIP_TABLE + "." + DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY + "<?" +
				" AND (" + DatabaseHelper.COLUMN_ITEM_HTML + " IS NULL OR " + DatabaseHelper.COLUMN_ITEM_THUMBNAIL + " IS NULL)";
		String[] selectionArgs = new String[] {Integer.toString(numItems)};
		return queryBuilder.query(database.getDatabase(), projection, selection, selectionArgs, null, null, null);
	}
	
	private void insertItem(ContentValues values, String category) {
		long id = -1; // will hold the id of the item, -1 for now to be safe
		// retrieve useful stuff from the content values
		int priority = values.getAsInteger(DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY);
		values.remove(DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY);
		String title = values.getAsString(DatabaseHelper.COLUMN_ITEM_TITLE);
		
		// query to see if this item is already in the database
		String[] projection = new String[] { DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_ITEM_TITLE };
		String selection = DatabaseHelper.COLUMN_ITEM_URL + "=?";
		String[] selectionArgs = new String[] { values.getAsString(DatabaseHelper.COLUMN_ITEM_URL) };
		Cursor cursor = database.query(DatabaseHelper.ITEM_TABLE, projection, selection, selectionArgs, null);
		
		// check if any rows were returned, null means no rows
		if (cursor == null) {
			// insert the items
			database.insert(DatabaseHelper.ITEM_TABLE, values); // perform the insert operation
		}
		else if (cursor.getCount() == 1) {
			// this item exists
			cursor.moveToFirst();
			id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID)); // save the id
			// test to see if the title has changed
			if (!cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_TITLE)).equals(title)) {
				values.clear();
				// update the row
				values.put(DatabaseHelper.COLUMN_ITEM_TITLE, title);
				values.putNull(DatabaseHelper.COLUMN_ITEM_HTML);
				values.putNull(DatabaseHelper.COLUMN_ITEM_THUMBNAIL);
				selection = DatabaseHelper.COLUMN_ITEM_ID + "=?";
				database.update(DatabaseHelper.ITEM_TABLE, values, selection, new String[] { Long.toString(id) });
			}
		}
		cursor.close();
		
		// associate the item with its category
		values.clear();
		values.put(DatabaseHelper.COLUMN_RELATIONSHIP_CATEGORY_NAME, category);
		values.put(DatabaseHelper.COLUMN_RELATIONSHIP_ITEM_ID, id);
		values.put(DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY, priority);
		
		// insert it, if the required method doesn't exist, use the old one
		if (methodInsertWithConflictExists) {
			// FIXME performance: shouldn't replace every time
			WrapBackwards.insertWithOnConflict(database, DatabaseHelper.RELATIONSHIP_TABLE, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
		else {
			// use an alternative method
			try {
				database.insertOrThrow(DatabaseHelper.RELATIONSHIP_TABLE, values);
			} catch (SQLiteConstraintException e) {
				// this item obviously already exists, replace it instead
				database.replace(DatabaseHelper.RELATIONSHIP_TABLE, values);
			} catch (SQLException e) {
				// TODO handle this type of exception
			}
		}
	}
	
	private int updateItem(ContentValues values, int id) {
		String selection = DatabaseHelper.COLUMN_ITEM_ID + "=?";
		String[] selectionArgs = new String[] {Integer.toString(id)};
		return database.update(DatabaseHelper.ITEM_TABLE, values, selection, selectionArgs);
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// try and match the uri
		switch (uriMatcher.match(uri)){
		case ITEMS:
			// just run the delete query on the database items table
			return database.delete(DatabaseHelper.ITEM_TABLE, selection, selectionArgs);
		}
		return 0;
	}
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// try and match the uri
		switch (uriMatcher.match(uri)) {
		case ITEMS:
			// insert the provided item
			String category = uri.getLastPathSegment();
			insertItem(values, category);
			break;
		case CATEGORIES:
			//insert the provided item
			database.insert(DatabaseHelper.CATEGORY_TABLE, values);
			break;
		}
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// try and match the queried uri
		switch (uriMatcher.match(uri)) {
		case CATEGORIES:
			// query the database for all the categories
			if (selection == null) {
				throw new IllegalArgumentException("Uri requires selection: " + uri.toString());
			}
			return getCategories(projection, selection, selectionArgs, sortOrder);
		case ENABLED_CATEGORIES:
			// query the database for enabled categories
			if (selection == null) {
				throw new IllegalArgumentException("Uri requires selection: " + uri.toString());
			}
			return getEnabledCategories(projection, sortOrder);
		case ITEMS:
			// query the database for items
			return this.getItems(projection, selection, selectionArgs, sortOrder);
		case ITEM_BY_ID:
			// query the database for this specific item
			int id = Integer.parseInt(uri.getLastPathSegment());
			return getItem(projection, id);
		case ITEMS_BY_CATEGORY:
			// query the database for items in this category
			String category = uri.getLastPathSegment();
			return getItems(projection, category, sortOrder);
		case UNDOWNLOADED_ITEMS:
			// query the database for undownloaded items of this type
			int numItems = Integer.parseInt(uri.getLastPathSegment());
			return getUndownloadedItems(projection, numItems);
		default:
			throw new IllegalArgumentException("Unknown uri: " + uri.toString());
		}
	}
	
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch(uriMatcher.match(uri)){
		case ITEM_BY_ID:
			int id = Integer.parseInt(uri.getLastPathSegment());
			return updateItem(values, id);
		}
		return 0;
	}
	
	@Override
	public boolean onCreate() {
		// initialise the database
		database = new DatabaseHelper(this.getContext());
		// check compatibility
		checkCompatibility();
		
		return true;
	}
	
	public DatabaseProvider() {
		// TODO Auto-generated constructor stub
	}
	
}
