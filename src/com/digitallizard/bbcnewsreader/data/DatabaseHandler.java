/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.data;

import java.util.ArrayList;
import java.util.Date;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.method.MovementMethod;

import com.digitallizard.bbcnewsreader.NewsItem;
import com.digitallizard.bbcnewsreader.R;

public class DatabaseHandler extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "bbcnewsreader.db";
	private static final int DATABASE_VERSION = 2;
	private static final String ITEM_TABLE = "items";
	private static final String CATEGORY_TABLE = "categories";
	private static final String ITEM_CATEGORY_TABLE = "categories_items";
	private static final String CREATE_ITEM_TABLE = "CREATE TABLE " + ITEM_TABLE + "(item_Id integer PRIMARY KEY," + "title varchar(255), "
			+ "description varchar(255), " + "link varchar(255) UNIQUE, " + "pubdate int, " + "html blob, " + "image blob, " + "thumbnail blob,"
			+ "thumbnailurl varchar(255))";
	private static final String CREATE_CATEGORY_TABLE = "CREATE TABLE " + CATEGORY_TABLE + "(category_Id integer PRIMARY KEY," + "name varchar(255),"
			+ "enabled int," + "url varchar(255))";
	private static final String CREATE_RELATIONSHIP_TABLE = "CREATE TABLE " + ITEM_CATEGORY_TABLE + "(categoryName varchar(255), " + "itemId INT,"
			+ "priority int," + "PRIMARY KEY (categoryName, itemId))";
	public static final String COLUMN_HTML = "html";
	public static final String COLUMN_THUMBNAIL = "thumbnail";
	public static final String COLUMN_IMAGE = "image";
	public static final int COLUMN_UNDOWNLOADED_ARTICLES = 0;
	public static final int COLUMN_UNDOWNLOADED_THUMBNAILS = 1;
	private Context context;
	private SQLiteDatabase db;
	private long clearOutAgeMilliSecs; // the number of days to keep news items
	private boolean methodInsertWithConflictExists; // used to determine if the required method exists
	private ContentResolver contentResolver;
	
	/**
	 * Inserts an RSSItem into the items table, then creates an entry in the relationship table between it and its category, ONLY if it is more recent
	 * than a month.
	 * 
	 * @param title
	 *            News item's Title as String
	 * @param description
	 *            News item's Description as String
	 * @param link
	 *            News item's link as String
	 * @param pubdate
	 *            News item's published data as String
	 * @param category
	 *            News item's category as String
	 */
	public void insertItem(String title, String description, String category, Date pubdate, String url, String thumbnailUrl, int priority) {
		// convert the date into a timestamp
		long timestamp = pubdate.getTime();
		Date now = new Date();
		
		// check if this news is older than we want to store
		if (timestamp < (now.getTime() - clearOutAgeMilliSecs)) {
			// bail here, don't insert it
			return;
		}
		
		// request an insert
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS_BY_CATEGORY, category);
		ContentValues values = new ContentValues(5);
		values.put(DatabaseHelper.COLUMN_ITEM_TITLE, title);
		values.put(DatabaseHelper.COLUMN_ITEM_PUBDATE, timestamp);
		values.put(DatabaseHelper.COLUMN_ITEM_URL, url);
		values.put(DatabaseHelper.COLUMN_ITEM_THUMBNAIL_URL, thumbnailUrl);
		values.put(DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY, priority);
		contentResolver.insert(uri, values);
	}
	
	public void addHtml(int itemId, byte[] html) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		ContentValues values = new ContentValues(1);
		values.put(DatabaseHelper.COLUMN_ITEM_HTML, html);
		contentResolver.update(uri, values, null, null);
	}
	
	public byte[] getHtml(int itemId) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		Cursor cursor = contentResolver.query(uri, new String[] { DatabaseHelper.COLUMN_ITEM_HTML }, null, null, null);
		cursor.moveToFirst();
		byte[] html = cursor.getBlob(0);
		cursor.close();
		return html;
	}
	
	public void addImage(int itemId, byte[] image) {
		// currently does nothing
	}
	
	public byte[] getImage(int itemId) {
		// currently does nothing
		return null;
	}
	
	public void addThumbnail(int itemId, byte[] thumbnail) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		ContentValues values = new ContentValues(1);
		values.put(DatabaseHelper.COLUMN_ITEM_THUMBNAIL, thumbnail);
		contentResolver.update(uri, values, null, null);
	}
	
	public byte[] getThumbnail(int itemId) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		Cursor cursor = contentResolver.query(uri, new String[] { DatabaseHelper.COLUMN_ITEM_THUMBNAIL }, null, null, null);
		cursor.moveToFirst();
		byte[] thumbnail = cursor.getBlob(0);
		cursor.close();
		return thumbnail;
	}
	
	public String getUrl(int itemId) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		Cursor cursor = contentResolver.query(uri, new String[] { DatabaseHelper.COLUMN_ITEM_URL }, null, null, null);
		cursor.moveToFirst();
		String url = cursor.getString(0);
		cursor.close();
		return url;
	}
	
	public String getThumbnailUrl(int itemId) {
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
		Cursor cursor = contentResolver.query(uri, new String[] { DatabaseHelper.COLUMN_ITEM_THUMBNAIL_URL }, null, null, null);
		cursor.moveToFirst();
		String url = cursor.getString(0);
		cursor.close();
		return url;
	}
	
	/**
	 * Fetches all the undownloaded items from the last "days" days. Returns an array containing the item Ids of all these items
	 * 
	 * @param days
	 *            Number of days into the past to return undownloaded items for (Using timestamp from entry)
	 * @return A 2d int[2][n], where 2 is the type of item and n is the number of undownloaded items of that type. type is either 0 or 1 for html,
	 *         and thumbnail respectively.
	 */
	public Integer[][] getUndownloaded(int numItems) {
		// query the content provider for undownloaded items
		Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_UNDOWNLOADED_ITEMS, Integer.toString(numItems));
		String[] projection = new String[] {DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_ITEM_HTML, DatabaseHelper.COLUMN_ITEM_THUMBNAIL};
		Cursor cursor = contentResolver.query(uri, projection, null, null, null);
		
		// get the column name index
		int id = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID);
		int html = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_HTML);
		int thumbnail = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_THUMBNAIL);
		
		// create lists to save the arrays to
		ArrayList<Integer> undownloadedArticles = new ArrayList<Integer>();
		ArrayList<Integer> undownloadedThumbnails = new ArrayList<Integer>();
		
		// loop through and save what needs to be loaded
		while(cursor.moveToNext()){
			// check if we need to load this article
			if(cursor.isNull(html)){
				undownloadedArticles.add(new Integer(cursor.getInt(id)));
			}
			// check if we need to load this thumbnail
			if(cursor.isNull(thumbnail)){
				undownloadedThumbnails.add(new Integer(cursor.getInt(id)));
			}
		}
		
		cursor.close();
		
		// convert the array lists into a 2d array
		Integer[][] values = new Integer[2][];
		values[COLUMN_UNDOWNLOADED_ARTICLES] = undownloadedArticles.toArray(new Integer[undownloadedArticles.size()]);
		values[COLUMN_UNDOWNLOADED_THUMBNAILS] = undownloadedThumbnails.toArray(new Integer[undownloadedThumbnails.size()]);
		
		return values;
	}
	
	/**
	 * Fetches all the undownloaded items from the last "days" days. Returns an array containing the item Ids of all these items
	 * 
	 * @param category
	 *            The category to retrieve undownloaded items from
	 * @param days
	 *            Number of days into the past to return undownloaded items for (Using timestamp from entry)
	 * @return A 2d int[n], where n is the number of undownloaded items.
	 */
	public Integer[] getUndownloaded(String category, String column, int numItems) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		queryBuilder.setDistinct(true);
		queryBuilder.setTables("items JOIN categories_items ON items.item_Id=categories_items.itemId");
		String[] selectionArgs = new String[] { "item_Id", "items." + column };
		String whereStatement = "categories_items.categoryName=?";
		String[] whereArgs = new String[] { category };
		Cursor cursor = queryBuilder.query(db, selectionArgs, whereStatement, whereArgs, null, null, "categories_items.priority ASC",
				Integer.toString(numItems));
		// Log.v("database", "query: "+queryBuilder.buildQuery(selectionArgs, whereStatement, whereArgs, null, null, "pubdate DESC",
		// Integer.toString(numItems)));
		
		ArrayList<Integer> unloadedItems = new ArrayList<Integer>();
		
		// loop through and store the ids of the articles that need to be loaded
		for (int i = 0; i < cursor.getCount(); i++) {
			cursor.moveToNext();
			// check if we need to download this
			if (cursor.isNull(1)) {
				unloadedItems.add(cursor.getInt(0));
			}
		}
		
		cursor.close();
		
		return unloadedItems.toArray(new Integer[unloadedItems.size()]);
	}
	
	/**
	 * Inserts a category into the category table.
	 * 
	 * @param name
	 *            Name of the category as String
	 * @param enabled
	 *            Whether the RSSFeed should be fetched as Boolean
	 */
	public void insertCategory(String name, boolean enabled, String url) {
		Uri uri = DatabaseProvider.CONTENT_URI_CATEGORIES;
		int enabledInt = (enabled) ? 1 : 0;
		ContentValues values = new ContentValues(3);
		values.put(DatabaseHelper.COLUMN_CATEGORY_NAME, name);
		values.put(DatabaseHelper.COLUMN_CATEGORY_ENABLED, enabledInt);
		values.put(DatabaseHelper.COLUMN_CATEGORY_URL, url);
		contentResolver.insert(uri, values);
		db.insert(CATEGORY_TABLE, null, values);
	}
	
	/**
	 * Takes a category and returns all the title, description ,link and item_Id of all the items related to it. Returns null if no items exists
	 * 
	 * @param category
	 *            The Case-sensitive name of the category
	 * @param limit
	 *            for the number of items to return
	 * @return NewsItem[]
	 */
	public NewsItem[] getItems(String category, int limit) {
		// ask the content provider for the items
		Uri uri = DatabaseProvider.CONTENT_URI_ITEMS;
		String[] projection = new String[] {DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_ITEM_TITLE, DatabaseHelper.COLUMN_ITEM_DESCRIPTION, 
				DatabaseHelper.COLUMN_ITEM_URL, DatabaseHelper.COLUMN_ITEM_THUMBNAIL};
		String sortOrder = DatabaseHelper.RELATIONSHIP_TABLE + "." + DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY + " ASC";
		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		
		// load the column names
		int id = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID);
		int title = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_TITLE);
		int description = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_DESCRIPTION);
		int url = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_URL);
		int thumbnail = cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_THUMBNAIL);
		
		// load the items into an array
		NewsItem[] items = new NewsItem[cursor.getCount()];
		
		while(cursor.moveToNext() && cursor.getPosition() < limit){
			NewsItem item = new NewsItem(); // initialize a new item			
			item.setId(cursor.getInt(id));
			item.setTitle(cursor.getString(title));
			item.setDescription(cursor.getString(description));
			item.setUrl(cursor.getString(url));
			item.setThumbnailBytes(cursor.getBlob(thumbnail));
			items[cursor.getCount()] = item; // add this item to the array
		}
		
		cursor.close();
		
		return items;
	}
	
	/**
	 * Queries the categories table for the enabled column of all rows, returning an array of booleans representing whether categories are enabled or
	 * not, sorted by category_Id.
	 * 
	 * @return boolean[] containing enabled column from categories table.
	 */
	public boolean[] getCategoryBooleans() {
		Uri uri = DatabaseProvider.CONTENT_URI_CATEGORIES;
		String[] projection = new String[] { DatabaseHelper.COLUMN_CATEGORY_ENABLED };
		Cursor cursor = contentResolver.query(uri, projection, null, null, DatabaseHelper.COLUMN_CATEGORY_ID);
		boolean[] enabledCategories = new boolean[cursor.getCount()];
		while (cursor.moveToNext()) {
			if (cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_CATEGORY_ENABLED)) == 0) {
				enabledCategories[cursor.getPosition() - 1] = false;
			}
			else {
				enabledCategories[cursor.getPosition() - 1] = true;
			}
		}
		cursor.close();
		return enabledCategories;
	}
	
	/**
	 * Returns the links and names of all the categories that are enabled.
	 * 
	 * @return A string[][] containing the String urls in [0] and String names in [1].
	 */
	public String[][] getEnabledCategories() {
		Uri uri = DatabaseProvider.CONTENT_URI_ENABLED_CATEGORIES; // uri for enabled categories
		String[] projection = new String[] { DatabaseHelper.COLUMN_CATEGORY_URL, DatabaseHelper.COLUMN_CATEGORY_NAME };
		Cursor cursor = contentResolver.query(uri, projection, null, null, DatabaseHelper.COLUMN_CATEGORY_ID);
		String[][] categories = new String[2][cursor.getCount()];
		while (cursor.moveToNext()) {
			categories[0][cursor.getPosition() - 1] = cursor.getString(0);
			categories[1][cursor.getPosition() - 1] = cursor.getString(1);
		}
		cursor.close();
		return categories;
	}
	
	/**
	 * Sets the given category to the given boolean
	 * 
	 * @param category
	 *            The String category you wish to change.
	 * @param enabled
	 *            The boolean value you wish to set it to.
	 */
	public void setCategoryEnabled(String category, boolean enabled) {
		// update this category
		ContentValues values = new ContentValues(1);
		if (enabled) {
			values.put("enabled", 1);
		}
		else {
			values.put("enabled", 0);
		}
		// update the database with these values
		db.update(CATEGORY_TABLE, values, "name=?", new String[] { category });
	}
	
	/**
	 * Takes an array of booleans and sets the first n categories to those values. Where n is length of array
	 * 
	 * @param enabled
	 *            A boolean array of "enabled" values
	 */
	public void setEnabledCategories(boolean[] enabled) throws NullPointerException {
		ContentValues values = new ContentValues(1);
		for (int i = 0; i < enabled.length; i++) {
			if (enabled[i]) {
				values.put("enabled", 1);
			}
			else {
				values.put("enabled", 0);
			}
			db.update(CATEGORY_TABLE, values, "category_Id=?", new String[] { Integer.toString(i + 1) });
			values.clear();
		}
	}
	
	/**
	 * When called will remove all articles that are over the threshold to the second old. Then cleans up the relationship table. Possibly resource
	 * intensive.
	 */
	public void clearOld() {
		// delete items older than the threshold
		Date now = new Date();
		long cutoffTime = (now.getTime() - clearOutAgeMilliSecs);
		
		/*// FIXME Optimise?
		// Creates a java.util date object with current time
		// Subtracts one month in milliseconds and deletes all
		// items with a pubdate less than that value.
		Date now = new Date();
		long oldTime = (now.getTime() - clearOutAgeMilliSecs);
		Cursor cursor = db.query(ITEM_TABLE, new String[] { "item_Id" }, "pubdate<?", new String[] { Long.toString(oldTime) }, null, null, null);
		for (int i = 1; i <= cursor.getCount(); i++) {
			cursor.moveToNext();
			db.delete(ITEM_CATEGORY_TABLE, "itemId=?", new String[] { Integer.toString(cursor.getInt(0)) });
		}
		db.delete(ITEM_TABLE, "pubdate<?", new String[] { Long.toString(oldTime) });
		cursor.close();*/
	}
	
	/**
	 * Adds all the start categories from the XML
	 */
	public void addCategoriesFromXml() {
		try {
			String[] categoryNames = context.getResources().getStringArray(R.array.category_names);
			String[] categoryUrls = context.getResources().getStringArray(R.array.catergory_rss_urls);
			int[] categoryBooleans = context.getResources().getIntArray(R.array.category_default_booleans);
			for (int i = 0; i < categoryNames.length; i++) {
				boolean enabled = true;
				if (categoryBooleans[i] == 0) {
					enabled = false;
				}
				insertCategory(categoryNames[i], enabled, categoryUrls[i]);
			}
		} catch (NullPointerException e) {
			// Log.e("Database", "Categories XML is broken.");
		}
	}
	
	/**
	 * Checks whether there are any records in category
	 * 
	 * @return true or false
	 */
	public boolean isCreated() {
		try {
			getCategoryBooleans()[0] = true;
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Drops the entire database.
	 */
	public void dropTables() {
		db.execSQL("DROP TABLE " + ITEM_TABLE);
		db.execSQL("DROP TABLE " + CATEGORY_TABLE);
		db.execSQL("DROP TABLE " + ITEM_CATEGORY_TABLE);
	}
	
	/**
	 * Attempts to create the tables.
	 */
	public void createTables() {
		db.execSQL(CREATE_ITEM_TABLE);
		db.execSQL(CREATE_CATEGORY_TABLE);
		db.execSQL(CREATE_RELATIONSHIP_TABLE);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		// nothing to do
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// check what version to version upgrade we are performing
		if (oldVersion == 1 && newVersion == 2) {
			// drop tables
			db.execSQL("DROP TABLE " + ITEM_TABLE);
			db.execSQL("DROP TABLE " + ITEM_CATEGORY_TABLE);
			// create tables
			db.execSQL(CREATE_ITEM_TABLE);
			db.execSQL(CREATE_RELATIONSHIP_TABLE);
		}
		else {
			// reset everything to be sure
			dropTables();
			createTables();
		}
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// reset the database
		dropTables();
		createTables();
	}
	
	public void finish() {
		// close the database
		db.close();
		db = null;
	}
	
	void checkCompatibilty() {
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
	
	@Override
	protected void finalize() throws Throwable {
		// use try-catch to make sure we do not break super
		try {
			// make sure the database has been shutdown
			if (db != null) {
				db.close();
				db = null;
			}
		} finally {
			super.finalize();
		}
	}
	
	public DatabaseHandler(Context context, int clearOutAgeDays) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
		this.contentResolver = context.getContentResolver();
		
		this.clearOutAgeMilliSecs = (clearOutAgeDays * 24 * 60 * 60 * 1000);
		this.db = this.getWritableDatabase();
		
		// check compatibility with this version of Android
		checkCompatibilty();
	}
}
