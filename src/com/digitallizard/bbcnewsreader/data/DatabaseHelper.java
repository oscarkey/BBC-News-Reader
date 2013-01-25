/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

public class DatabaseHelper {
	/** constants **/
	private static final String DATABASE_NAME = "bbcnewsreader.db";
	private static final int DATABASE_VERSION = 3;
	
	// table names
	public static final String ITEM_TABLE = "items";
	public static final String CATEGORY_TABLE = "categories";
	public static final String RELATIONSHIP_TABLE = "categories_items";
	
	// column names
	public static final String COLUMN_CATEGORY_ID = "category_Id";
	public static final String COLUMN_CATEGORY_NAME = "name";
	public static final String COLUMN_CATEGORY_ENABLED = "enabled";
	public static final String COLUMN_CATEGORY_URL = "url";
	public static final String COLUMN_CATEGORY_PRIORITY = "priority";
	
	public static final String COLUMN_ITEM_ID = "item_Id";
	public static final String COLUMN_ITEM_TITLE = "title";
	public static final String COLUMN_ITEM_DESCRIPTION = "description";
	public static final String COLUMN_ITEM_PUBDATE = "pubdate";
	public static final String COLUMN_ITEM_URL = "link";
	public static final String COLUMN_ITEM_THUMBNAIL_URL = "thumbnailurl";
	public static final String COLUMN_ITEM_HTML = "html";
	public static final String COLUMN_ITEM_THUMBNAIL = "thumbnail";
	
	public static final String COLUMN_RELATIONSHIP_ITEM_ID = "itemId";
	public static final String COLUMN_RELATIONSHIP_CATEGORY_NAME = "categoryName";
	public static final String COLUMN_RELATIONSHIP_PRIORITY = "priority";
	
	/** variables **/
	DatabaseOpenHelper databaseOpenHelper;
	
	public Cursor query(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder, int limit) {
		// build a query
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(table);
		
		// work out the limit
		String stringLimit = Integer.toString(limit);
		if (limit == -1) {
			stringLimit = null;
		}
		
		// perform the query
		Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, sortOrder,
				stringLimit);
		
		// return the cursor if it suitable
		if (cursor == null) {
			return null;
		}
		else if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}
	
	public Cursor query(String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		// call the main function with limit as -1
		return query(table, projection, selection, selectionArgs, sortOrder, -1);
	}
	
	public long insert(String table, ContentValues values) {
		return getDatabase().insert(table, null, values);
	}
	
	public long insertWithOnConflict(String table, ContentValues values, int conflictAlgorithm) {
		return getDatabase().insertWithOnConflict(table, null, values, conflictAlgorithm);
	}
	
	public long insertOrThrow(String table, ContentValues values) throws SQLException {
		return getDatabase().insertOrThrow(table, null, values);
	}
	
	public int update(String table, ContentValues values, String selection, String[] selectionArgs) {
		return getDatabase().update(table, values, selection, selectionArgs);
	}
	
	public int updateWithOnConflict(String table, ContentValues values, String selection, String[] selectionArgs, int conflictAlgorithm) {
		return getDatabase().updateWithOnConflict(table, values, selection, selectionArgs, conflictAlgorithm);
	}
	
	public long replace(String table, ContentValues values) {
		return getDatabase().replace(table, null, values);
	}
	
	public int delete(String table, String selection, String selectionArgs[]) {
		return getDatabase().delete(table, selection, selectionArgs);
	}
	
	public void beginTransaction() {
		getDatabase().beginTransaction();
	}
	
	public void setTransactionSuccessful() {
		getDatabase().setTransactionSuccessful();
	}
	
	public void endTransaction() {
		getDatabase().endTransaction();
	}
	
	public SQLiteDatabase getDatabase() {
		return databaseOpenHelper.getWritableDatabase();
	}
	
	public DatabaseHelper(Context context) {
		databaseOpenHelper = new DatabaseOpenHelper(context);
	}
	
	private static class DatabaseOpenHelper extends SQLiteOpenHelper {
		
		@SuppressWarnings("unused")
		private final Context context;
		private SQLiteDatabase database;
		
		// define the tables
		private static final String CREATE_ITEM_TABLE = "CREATE TABLE " + ITEM_TABLE + "(item_Id integer PRIMARY KEY," + "title varchar(255), "
				+ "description varchar(255), " + "link varchar(255) UNIQUE, " + "pubdate int, " + "html blob, " + "image blob, " + "thumbnail blob,"
				+ "thumbnailurl varchar(255))";
		private static final String CREATE_CATEGORY_TABLE = "CREATE TABLE " + CATEGORY_TABLE + "(category_Id integer PRIMARY KEY,"
				+ "name varchar(255)," + "enabled int," + "url varchar(255), " + COLUMN_CATEGORY_PRIORITY
				+ " int)";
		private static final String CREATE_RELATIONSHIP_TABLE = "CREATE TABLE " + RELATIONSHIP_TABLE + "(categoryName varchar(255), " + "itemId INT,"
				+ "priority int," + "PRIMARY KEY (categoryName, itemId))";
		
		@Override
		public void onCreate(SQLiteDatabase database) {
			this.database = database;
			// create the tables
			this.database.execSQL(CREATE_ITEM_TABLE);
			this.database.execSQL(CREATE_CATEGORY_TABLE);
			this.database.execSQL(CREATE_RELATIONSHIP_TABLE);
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// check what version to version upgrade we are performing
			if (oldVersion == 1 && newVersion == 2) {
				// drop tables
				db.execSQL("DROP TABLE " + ITEM_TABLE);
				db.execSQL("DROP TABLE " + RELATIONSHIP_TABLE);
				// create tables
				db.execSQL(CREATE_ITEM_TABLE);
				db.execSQL(CREATE_RELATIONSHIP_TABLE);
			}
			else if(oldVersion == 2 && newVersion == 3) {
				// add the priority column to the category table
				db.execSQL("ALTER TABLE " + CATEGORY_TABLE + " ADD COLUMN " 
						+ COLUMN_CATEGORY_PRIORITY + " int");
			}
			else {
				// unsupported upgrade, reset everything
				db.execSQL("DROP TABLE " + ITEM_TABLE);
				db.execSQL("DROP TABLE " + CATEGORY_TABLE);
				db.execSQL("DROP TABLE " + RELATIONSHIP_TABLE);
				database.execSQL(CREATE_ITEM_TABLE);
				database.execSQL(CREATE_CATEGORY_TABLE);
				database.execSQL(CREATE_RELATIONSHIP_TABLE);
			}
		}
		
		@Override
		public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE " + ITEM_TABLE);
			db.execSQL("DROP TABLE " + CATEGORY_TABLE);
			db.execSQL("DROP TABLE " + RELATIONSHIP_TABLE);
			database.execSQL(CREATE_ITEM_TABLE);
			database.execSQL(CREATE_CATEGORY_TABLE);
			database.execSQL(CREATE_RELATIONSHIP_TABLE);
		}
		
		DatabaseOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}
	}
}
