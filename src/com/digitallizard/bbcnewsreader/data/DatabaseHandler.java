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
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.digitallizard.bbcnewsreader.NewsItem;
import com.digitallizard.bbcnewsreader.R;

public class DatabaseHandler extends SQLiteOpenHelper {

   private static final String DATABASE_NAME = "bbcnewsreader.db";
   private static final int DATABASE_VERSION = 2;
   private static final String ITEM_TABLE = "items";
   private static final String CATEGORY_TABLE = "categories";
   private static final String ITEM_CATEGORY_TABLE = "categories_items";
   private static final String CREATE_ITEM_TABLE = "CREATE TABLE " + ITEM_TABLE + 
   											  "(item_Id integer PRIMARY KEY," +
									          "title varchar(255), " +
									          "description varchar(255), " +
									          "link varchar(255) UNIQUE, " +
									          "pubdate int, " +
									          "html blob, " +
									          "image blob, " +
									          "thumbnail blob," +
									          "thumbnailurl varchar(255))";
   private static final String CREATE_CATEGORY_TABLE = "CREATE TABLE " + CATEGORY_TABLE +
									          "(category_Id integer PRIMARY KEY," +
									          "name varchar(255)," +
									          "enabled int," +
									          "url varchar(255))";
   private static final String CREATE_RELATIONSHIP_TABLE = "CREATE TABLE " + ITEM_CATEGORY_TABLE +
									          "(categoryName varchar(255), " +
									          "itemId INT," +
									          "priority int," +
									          "PRIMARY KEY (categoryName, itemId))";
   public static final String COLUMN_HTML = "html";
   public static final String COLUMN_THUMBNAIL = "thumbnail";
   public static final String COLUMN_IMAGE = "image";
   private Context context;
   private SQLiteDatabase db;
   private long clearOutAgeMilliSecs; //the number of days to keep news items
   private boolean methodInsertWithConflictExists; //used to determine if the required method exists
   private ContentResolver contentResolver;
   
   /**
    * Inserts an RSSItem into the items table, then creates an entry in the relationship
    * table between it and its category, ONLY if it is more recent than a month.
    * @param title News item's Title as String
    * @param description News item's Description as String
    * @param link News item's link as String
    * @param pubdate News item's published data as String
    * @param category News item's category as String
    */
   public void insertItem(String title, String description, String category, Date pubdate, String url, String thumbnailUrl, int priority){
	   //convert the date into a timestamp
	   long timestamp = pubdate.getTime();
	   Date now = new Date();
	   
	   //check if this news is older than we want to store
	   if(timestamp < (now.getTime() - clearOutAgeMilliSecs)){
		   //bail here, don't insert it
		   return;
	   }
	   
	   //query to see if this item is already in the database
	   Uri uri = DatabaseProvider.CONTENT_URI_ITEMS;
	   String[] projection = new String[] {DatabaseHelper.COLUMN_ITEM_ID, DatabaseHelper.COLUMN_ITEM_TITLE};
	   String selection = DatabaseHelper.COLUMN_ITEM_URL + "=?";
	   String[] selectionArgs = new String[] {url};
	   Cursor cursor = contentResolver.query(uri, projection, selection, selectionArgs, null);
	   
	   long id = -1; //will hold the id of the item, -1 for now to be safe
	   
	   //check if any rows were returned, null means no rows
	   if(cursor == null){
		   //insert the items
		   ContentValues values = new ContentValues(5);
		   values.put(DatabaseHelper.COLUMN_ITEM_TITLE, title);
		   values.put(DatabaseHelper.COLUMN_ITEM_DESCRIPTION, description);
		   values.put(DatabaseHelper.COLUMN_ITEM_PUBDATE, timestamp);
		   values.put(DatabaseHelper.COLUMN_ITEM_URL, url);
		   values.put(DatabaseHelper.COLUMN_ITEM_THUMBNAIL_URL, thumbnailUrl);
		   
		   contentResolver.insert(DatabaseProvider.CONTENT_URI_ITEMS, values); //perform the insert operation
	   }
	   else if(cursor.getCount() == 1){
		   //this item exists
		   cursor.moveToFirst();
		   id = (long)cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_ID)); //save the id
		   
		   //test to see if the title has changed
		   if(!cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_ITEM_TITLE)).equals(title)){
			   ContentValues values = new ContentValues(3);
			   //update the row
			   values.put(DatabaseHelper.COLUMN_ITEM_TITLE, values.getAsString(DatabaseHelper.COLUMN_ITEM_TITLE));
			   values.putNull(DatabaseHelper.COLUMN_ITEM_HTML);
			   values.putNull(DatabaseHelper.COLUMN_ITEM_THUMBNAIL);
			   selection = DatabaseHelper.COLUMN_ITEM_ID + "=?";
			   contentResolver.update(DatabaseProvider.CONTENT_URI_ITEMS, 
					   values, selection, new String[] {Long.toString(id)});
		   }
	   }
	   cursor.close();
	   
	   //associate this item with its category
	   uri = DatabaseProvider.CONTENT_URI_ITEMS_BY_CATEGORY;
	   values.clear(); //clear the content values
	   values.put(DatabaseHelper.COLUMN_RELATIONSHIP_CATEGORY_NAME, category);
	   values.put(DatabaseHelper.COLUMN_RELATIONSHIP_ITEM_ID, id);
	   values.put(DatabaseHelper.COLUMN_RELATIONSHIP_PRIORITY, priority);
   }
   
   public void addHtml(int itemId, byte[] html){
	   ContentValues cv = new ContentValues(1);
	   cv.put("html", html);
	   db.update(ITEM_TABLE, cv, "item_Id=?", new String[]{Integer.toString(itemId)});
   }
   
   public byte[] getHtml(int itemId){
	   Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
	   Cursor cursor = contentResolver.query(uri, new String[]{DatabaseHelper.COLUMN_ITEM_HTML}, null, null, null);
	   cursor.moveToFirst();
	   byte[] html = cursor.getBlob(0);
	   cursor.close();
	   return html;
   }
   
   public void addImage(int itemId, byte[] image){
	   //currently does nothing
   }
   
   public byte[] getImage(int itemId){
	   //currently does nothing
	   return null;
   }
   
   public void addThumbnail(int itemId, byte[] thumbnail){
	   ContentValues cv = new ContentValues(1);
	   cv.put("thumbnail", thumbnail);
	   String itemIdString = Integer.toString(itemId);
	   db.update(ITEM_TABLE, cv, "item_Id=?", new String[]{itemIdString});
   }
   
   public byte[] getThumbnail(int itemId){
	   Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
	   Cursor cursor = contentResolver.query(uri, new String[]{DatabaseHelper.COLUMN_ITEM_THUMBNAIL}, null, null, null);
	   cursor.moveToFirst();
	   byte[] thumbnail = cursor.getBlob(0);
	   cursor.close();
	   return thumbnail;
   }
   
   public String getUrl(int itemId){
	   Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
	   Cursor cursor = contentResolver.query(uri, new String[]{DatabaseHelper.COLUMN_ITEM_URL}, null, null, null);
	   cursor.moveToFirst();
	   String url = cursor.getString(0);
	   cursor.close();
	   return url;
   }
   
   public String getThumbnailUrl(int itemId){
	   Uri uri = Uri.withAppendedPath(DatabaseProvider.CONTENT_URI_ITEMS, Integer.toString(itemId));
	   Cursor cursor = contentResolver.query(uri, new String[]{DatabaseHelper.COLUMN_ITEM_THUMBNAIL_URL}, null, null, null);
	   cursor.moveToFirst();
	   String url = cursor.getString(0);
	   cursor.close();
	   return url;
   }
   
   /**
    * Fetches all the undownloaded items from the last "days" days. Returns an array containing the item Ids of all these items
    * 
    * @param days Number of days into the past to return undownloaded items for (Using timestamp from entry)
    * @return A 2d int[3][n], where 3 is the type of item and n is the number of undownloaded items of that type.
    * 			type is either 0, 1 or 2 for html, thumbnail or image respectively.
    */
   public Integer[][] getUndownloaded(int days) {
	   Date now = new Date();
	   long curTime = now.getTime();
	   long timeComparison = curTime-86400000L*days;
	   String timeComparisonS = Long.toString(timeComparison);
	   
	   Cursor cursorArticles = db.query(ITEM_TABLE, new String[]{"item_Id"}, "html IS NULL AND pubdate>?", new String[] {timeComparisonS},null,null,null);
	   Cursor cursorThumbnails = db.query(ITEM_TABLE, new String[]{"item_Id"}, "thumbnail IS NULL AND pubdate>?", new String[] {timeComparisonS},null,null,null);
	   Cursor cursorImages = db.query(ITEM_TABLE, new String[]{"item_Id"}, "image IS NULL AND pubdate>?", new String[] {timeComparisonS},null,null,null);
	   	   
	   ArrayList<Integer> unloadedArticles = new ArrayList<Integer>();
	   ArrayList<Integer> unloadedThumbnails = new ArrayList<Integer>();
	   ArrayList<Integer> unloadedImages = new ArrayList<Integer>();
	   
	   
	   //loop through and store the ids of the articles that need to be loaded
	   for(int i=0; i < cursorArticles.getCount(); i++){
		   cursorArticles.moveToNext();
		   unloadedArticles.add(cursorArticles.getInt(0));
	   }
	   //loop through and store the ids of the thumbnails that need to be loaded
	   for(int i=0; i < cursorThumbnails.getCount(); i++){
		   cursorThumbnails.moveToNext();
		   unloadedThumbnails.add(cursorThumbnails.getInt(0));
	   }
	   //loop through and store the ids of the images that need to be loaded
	   for(int i=0; i < cursorImages.getCount(); i++){
		   cursorImages.moveToNext();
		   unloadedImages.add(cursorImages.getInt(0));
	   }
	   
	   cursorArticles.close();
	   cursorThumbnails.close();
	   cursorImages.close();
	   
	   Integer[][] values = new Integer[3][];
	   values[0] = unloadedArticles.toArray(new Integer[unloadedArticles.size()]);
	   values[1] = unloadedThumbnails.toArray(new Integer[unloadedThumbnails.size()]);
	   values[2] = unloadedImages.toArray(new Integer[unloadedImages.size()]);
	   return values;
   }

   /**
    * Fetches all the undownloaded items from the last "days" days. Returns an array containing the item Ids of all these items
    * 
    * @param category The category to retrieve undownloaded items from
    * @param days Number of days into the past to return undownloaded items for (Using timestamp from entry)
    * @return A 2d int[n], where n is the number of undownloaded items.
    */
   public Integer[] getUndownloaded(String category, String column, int numItems) {
	   SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	   queryBuilder.setDistinct(true);
	   queryBuilder.setTables("items JOIN categories_items ON items.item_Id=categories_items.itemId");
	   String[] selectionArgs = new String[]{"item_Id", "items."+column};
	   String whereStatement = "categories_items.categoryName=?";
	   String[] whereArgs = new String[]{category};
	   Cursor cursor = queryBuilder.query(db, selectionArgs, whereStatement, whereArgs, null, null, "categories_items.priority ASC", Integer.toString(numItems));
	   //Log.v("database", "query: "+queryBuilder.buildQuery(selectionArgs, whereStatement, whereArgs, null, null, "pubdate DESC", Integer.toString(numItems)));
	   
	   ArrayList<Integer> unloadedItems = new ArrayList<Integer>();
	   
	   //loop through and store the ids of the articles that need to be loaded
	   for(int i=0; i < cursor.getCount(); i++){
		   cursor.moveToNext();
		   //check if we need to download this
		   if(cursor.isNull(1))
			   unloadedItems.add(cursor.getInt(0));
	   }
	   
	   cursor.close();
	   
	   return unloadedItems.toArray(new Integer[unloadedItems.size()]);
   }
   
   /**
    * Inserts a category into the category table.
    * @param name Name of the category as String
    * @param enabled Whether the RSSFeed should be fetched as Boolean
    */
   public void insertCategory(String name, boolean enabledB, String url){
	   int enabledI = (enabledB) ? 1:0;
	   ContentValues cv = new ContentValues(3);
	   cv.put("name", name);
	   cv.put("enabled", enabledI);
	   cv.put("url", url);
	   db.insert(CATEGORY_TABLE, null, cv);
   }
   
   /**
    * Takes a category and returns all the title, description ,link and item_Id of all
    * the items related to it.
    * Returns null if no items exists
    * @param category The Case-sensitive name of the category
    * @param limit for the number of items to return
    * @return NewsItem[]
    */
   public NewsItem[] getItems(String category, int limit){
	   //build a query to find the items
	   SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
	   queryBuilder.setDistinct(true);
	   queryBuilder.setTables("items JOIN categories_items ON items.item_Id=categories_items.itemId");
	   String[] selectionArgs = new String[]{"title", "description", "link", "item_Id", "thumbnail"};
	   String whereStatement = "categories_items.categoryName=?";
	   String[] whereArgs = new String[]{category};
	   Cursor cursor = queryBuilder.query(db, selectionArgs, whereStatement, whereArgs, null, null, "categories_items.priority ASC", Integer.toString(limit));
	   
	   //load these items into an array
	   NewsItem[] items = new NewsItem[cursor.getCount()];
	   for(int i = 0; i < cursor.getCount(); i++){
		   cursor.moveToNext();
		   //create a new news item object
		   items[i] = new NewsItem(Integer.parseInt(cursor.getString(3)), cursor.getString(0), cursor.getString(1), cursor.getString(2),
				   cursor.getBlob(4));
	   }
	   cursor.close();
	   return items;
   }
   
   /**
    * Queries the categories table for the enabled column of all rows,
    * returning an array of booleans representing whether categories are enabled or not,
    * sorted by category_Id.
    * @return boolean[] containing enabled column from categories table.
    */
   public boolean[] getCategoryBooleans(){
	   Uri uri = DatabaseProvider.CONTENT_URI_CATEGORIES;
	   String[] projection = new String[] {DatabaseHelper.COLUMN_CATEGORY_ENABLED};
	   Cursor cursor = contentResolver.query(uri, projection, null, null, DatabaseHelper.COLUMN_CATEGORY_ID);
	   boolean[] enabledCategories = new boolean[cursor.getCount()];
	   cursor.moveToFirst();
	   while(cursor.moveToNext()){
		   if(cursor.getInt(0) == 0)
			   enabledCategories[cursor.getPosition() - 1] = false;
		   else
			   enabledCategories[cursor.getPosition() - 1] = true;
	   }
	   cursor.close();
	   return enabledCategories;
   }
   
   /**
    * Returns the links and names of all the categories that are enabled.
    * @return A string[][] containing the String urls in [0] and String names in [1].
    */
   public String[][] getEnabledCategories(){
	   Uri uri = DatabaseProvider.CONTENT_URI_ENABLED_CATEGORIES; //uri for enabled categories
	   String[] projection = new String[] {DatabaseHelper.COLUMN_CATEGORY_URL, DatabaseHelper.COLUMN_CATEGORY_NAME};
	   Cursor cursor = contentResolver.query(uri, projection, null, null, DatabaseHelper.COLUMN_CATEGORY_ID);
	   String[][] categories = new String[2][cursor.getCount()];
	   cursor.moveToFirst();
	   while(cursor.moveToNext()){
		   categories[0][cursor.getPosition() - 1] = cursor.getString(0);
		   categories[1][cursor.getPosition() - 1] = cursor.getString(1);
	   }
	   cursor.close();
	   return categories;
   }
   
   /**
    * Sets the given category to the given boolean
    * @param category The String category you wish to change.
    * @param enabled The boolean value you wish to set it to.
    */
   public void setCategoryEnabled(String category, boolean enabled){
	   //update this category
	   ContentValues values = new ContentValues(1);
	   if(enabled)
		   values.put("enabled", 1);
	   else
		   values.put("enabled", 0);
	   //update the database with these values
	   db.update(CATEGORY_TABLE, values, "name=?", new String[]{category});
   }
   
   /**
    * Takes an array of booleans and sets the first n categories
    * to those values. Where n is length of array
    * @param enabled A boolean array of "enabled" values
    */
   public void setEnabledCategories(boolean[] enabled) throws NullPointerException {
	   ContentValues values = new ContentValues(1);
	   for(int i = 0; i < enabled.length; i++){
		   if(enabled[i])
			   values.put("enabled", 1);
		   else
			   values.put("enabled", 0);
		   db.update(CATEGORY_TABLE, values, "category_Id=?", new String[]{Integer.toString(i+1)});
		   values.clear();
	   }
   }
   
   /**
    * When called will remove all articles that are
    * over one month to the second old. Then cleans up
    * the relationship table. Possibly resource intensive.
    */
   public void clearOld()
   {
	   
	   //FIXME Optimise?
	   //Creates a java.util date object with current time
	   //Subtracts one month in milliseconds and deletes all
	   //items with a pubdate less than that value.
	   Date now = new Date();
	   long oldTime = (now.getTime() - clearOutAgeMilliSecs);
	   Cursor cursor=db.query(ITEM_TABLE,new String[]{"item_Id"},"pubdate<?",new String[]{Long.toString(oldTime)},null,null,null);
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   db.delete(ITEM_CATEGORY_TABLE,"itemId=?",new String[]{Integer.toString(cursor.getInt(0))});
	   }
	   db.delete(ITEM_TABLE,"pubdate<?",new String[]{Long.toString(oldTime)});
	   cursor.close();
   }
   
   /**
    * Adds all the start categories from the XML
    */
   public void addCategoriesFromXml(){
	   try{
		   String[] categoryNames = context.getResources().getStringArray(R.array.category_names);
		   String[] categoryUrls = context.getResources().getStringArray(R.array.catergory_rss_urls);
		   int[] categoryBooleans = context.getResources().getIntArray(R.array.category_default_booleans);
		   for(int i = 0; i < categoryNames.length; i++){
			   boolean enabled = true;
			   if(categoryBooleans[i] == 0){
				   enabled = false;
			   }
			   insertCategory(categoryNames[i], enabled, categoryUrls[i]);
		   }
	   }
	   catch(NullPointerException e){
		   //Log.e("Database", "Categories XML is broken.");
	   }
   }
   
   /**
    * Checks whether there are any records in category
    * @return true or false
    */
   public boolean isCreated(){
	   try{
		   getCategoryBooleans()[0]=true;
		   return true;
	   }
	   catch(Exception e){
		   return false;
	   }
   }
   
   /**
    * Drops the entire database.
    */
   public void dropTables(){
	   db.execSQL("DROP TABLE "+ITEM_TABLE);
	   db.execSQL("DROP TABLE "+CATEGORY_TABLE);
	   db.execSQL("DROP TABLE "+ITEM_CATEGORY_TABLE);
   }
   
   /**
    * Attempts to create the tables.
    */
   public void createTables(){
	   db.execSQL(CREATE_ITEM_TABLE);
	   db.execSQL(CREATE_CATEGORY_TABLE);
	   db.execSQL(CREATE_RELATIONSHIP_TABLE);
   }
   
   @Override
   public void onCreate(SQLiteDatabase db){
	   //nothing to do
   }
   
   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
	   //check what version to version upgrade we are performing
	   if(oldVersion == 1 && newVersion == 2){
		   //drop tables
		   db.execSQL("DROP TABLE "+ITEM_TABLE);
		   db.execSQL("DROP TABLE "+ITEM_CATEGORY_TABLE);
		   //create tables
		   db.execSQL(CREATE_ITEM_TABLE);
		   db.execSQL(CREATE_RELATIONSHIP_TABLE);
	   }
	   else{
		   //reset everything to be sure
		   dropTables();
		   createTables();
	   }
   }
   
   public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
	   //reset the database
	   dropTables();
	   createTables();
   }
   
   public void finish(){
	   //close the database
	   db.close();
	   db = null;
   }
   
   void checkCompatibilty(){
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
   
   protected void finalize() throws Throwable {
	   //use try-catch to make sure we do not break super
	   try{
		   //make sure the database has been shutdown
		   if(db != null){
			   db.close();
			   db = null;
		   }
	   } finally {
		   super.finalize();
	   }
   }
   
   public DatabaseHandler(Context context, int clearOutAgeDays){
	   super(context, DATABASE_NAME, null, DATABASE_VERSION);
	   this.context = context;
	   this.contentResolver = context.getContentResolver();
	   
	   this.clearOutAgeMilliSecs = (long)(clearOutAgeDays * 24 * 60 * 60 * 1000);
	   this.db = this.getWritableDatabase();
	   
	   //check compatibility with this version of Android
	   checkCompatibilty();
   }
}
