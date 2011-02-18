/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.data;

import java.util.Date;
import java.sql.Blob;
import java.text.SimpleDateFormat;

import com.digitallizard.bbcnewsreader.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler {

   private static final String DATABASE_NAME = "bbcnewsreader.db";
   private static final int DATABASE_VERSION = 1;
   private static final String ITEM_TABLE = "items";
   private static final String CATEGORY_TABLE = "categories";
   private static final String ITEM_CATEGORY_TABLE = "categories_items";
   private static final String TABLE_CREATE ="CREATE TABLE " + ITEM_TABLE + 
   											  "(item_Id integer PRIMARY KEY," +
									          "title varchar(255), " +
									          "description varchar(255), " +
									          "link varchar(255), " +
									          "pubdate int, " +
									          "html text, " +
									          "image blob, " +
									          "thumbnail blob)";
   private static final String TABLE2_CREATE="CREATE TABLE " + CATEGORY_TABLE +
									          "(category_Id integer PRIMARY KEY," +
									          "name varchar(255)," +
									          "enabled int," +
									          "url varchar(255))";
   private static final String TABLE3_CREATE="CREATE TABLE " + ITEM_CATEGORY_TABLE +
									          "(categoryName varchar(255), " +
									          "itemId INT)";
   private Context context;
   private SQLiteDatabase db;
   public DatabaseHandler(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
   }
   /**
    * Inserts an RSSItem into the items table, then creates an entry in the relationship
    * table between it and its category, ONLY if it is more recent than a month.
    * @param title News item's Title as String
    * @param description News item's Description as String
    * @param link News item's link as String
    * @param pubdate News item's published data as String
    * @param category News item's category as String
    */
   public int insertItem(String title, String description, String link, String pubdate, String category)
   {
	   int itemId = -1;
	   //Formats the date of the item to Date object, then gets the UNIX TIMESTAMP from the Date.
	   SimpleDateFormat format=new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
	   long timestamp=0;
	   try
	   {
		   Date parsed=format.parse(pubdate);
		   timestamp=parsed.getTime();
	   }catch(Exception e)
	   {
		   //Log.v("ERROR",e.toString());
	   }
	   //FIXME Change time to a variable.
	   Boolean recent=false;
	   Date now=new Date();
	   if(timestamp>(now.getTime()-2629743000L))
		{
		   recent=true;
		}
	   //Compiles then executes the insertion of the item into the items database.
	   //Takes the rowid of the new record and uses it to get the item_id.
	   //Moves to first item in Cursor then inserts item_id and category into relationship table.
	   Cursor cursor=db.query(false,ITEM_TABLE,new String[]{"item_Id"},"title=?",new String[] {title},null,null,null,null);
	   ContentValues cv=null;
	   if(cursor.getCount()==0)
	   {
		   if(recent){
			   cv=new ContentValues(4);
			   cv.put("title",title);
			   cv.put("description",description);
			   cv.put("link",link);
			   cv.put("pubdate",timestamp);
			   long rowid=db.insert(ITEM_TABLE, null, cv);
			   cursor=db.query(false,ITEM_TABLE,new String[]{"item_Id"},"rowid=?",new String[] {Long.toString(rowid)},null,null,null, null);
			   
			   cursor.moveToNext();
			   itemId=cursor.getInt(0);
			   cv=new ContentValues(2);
			   cv.put("categoryName",category);
			   cv.put("itemId",itemId);
			   db.insert(ITEM_CATEGORY_TABLE, null, cv);
		   }
	   }
	   else
	   {
		   cursor.moveToNext();
		   itemId=cursor.getInt(0);
	   }
	   cursor.close();
	   return itemId;
   }
   public void addHtml(int itemId,String html)
   {
	   ContentValues cv=null;
	   cv=new ContentValues(1);
	   cv.put("html",html);
	   String itemIdString=Integer.toString(itemId);
	   db.update(ITEM_TABLE, cv, "item_Id=?", new String[]{itemIdString});
   }
   public String getHtml(int itemId)
   {
	   Cursor cursor;
	   String itemIdString=Integer.toString(itemId);
	   cursor=db.query(ITEM_TABLE, new String[]{"html"}, "item_Id=?", new String[] {itemIdString}, null, null, null);
	   cursor.moveToNext();
	   String html=cursor.getString(0);
	   cursor.close();
	   return html;
   }
   
   public void addImage(int itemId,byte[] image)
   {
	   ContentValues cv=null;
	   cv=new ContentValues(1);
	   cv.put("image",image);
	   String itemIdString=Integer.toString(itemId);
	   db.update(ITEM_TABLE, cv, "item_Id=?", new String[]{itemIdString});
   }
   public byte[] getImage(int itemId)
   {
	   Cursor cursor;
	   String itemIdString=Integer.toString(itemId);
	   cursor=db.query(ITEM_TABLE, new String[]{"image"}, "item_Id=?", new String[] {itemIdString}, null, null, null);
	   cursor.moveToNext();
	   byte[] image=cursor.getBlob(0);
	   cursor.close();
	   return image;
   }
   
   public void addThumbnail(int itemId,byte[] thumbnail)
   {
	   ContentValues cv=null;
	   cv=new ContentValues(1);
	   cv.put("thumbnail",thumbnail);
	   String itemIdString=Integer.toString(itemId);
	   db.update(ITEM_TABLE, cv, "item_Id=?", new String[]{itemIdString});
   }
   public byte[] getThumbnail(int itemId)
   {
	   Cursor cursor;
	   String itemIdString=Integer.toString(itemId);
	   cursor=db.query(ITEM_TABLE, new String[]{"thumbnail"}, "item_Id=?", new String[] {itemIdString}, null, null, null);
	   cursor.moveToNext();
	   byte[] thumbnail=cursor.getBlob(0);
	   cursor.close();
	   return thumbnail;
   }
   /**
    * Fetches all the undownloaded items from the last "days" days. Returns an array containing the item Ids of all these items
    * 
    * @param days Number of days into the past to return undownloaded items for (Using timestamp from entry)
    * @return A 2d int[n][2], where n is the number of undownloaded items, and each entry is {itemId,itemTag}.
    * 			itemTag is either 0, 1 or 2 for html, image or thumbnail respectively.
    */
   public int[][] getUndownloaded(int days)
   {
	   Cursor cursor1, cursor2, cursor3;
	   String emptyString = "";
	   
	   Date now=new Date();
	   long curTime=now.getTime();
	   long timeComparison= curTime-86400000L*days;
	   String timeComparisonS=Long.toString(timeComparison);
	   
	   cursor1=db.query(ITEM_TABLE, new String[]{"item_Id"}, "html=? AND timestamp>?", new String[] {emptyString,timeComparisonS},null,null,null);
	   cursor2=db.query(ITEM_TABLE, new String[]{"item_Id"}, "image=? AND timestamp>?", new String[] {emptyString,timeComparisonS},null,null,null);
	   cursor3=db.query(ITEM_TABLE, new String[]{"item_Id"}, "thumbnail=? AND timestamp>?", new String[] {emptyString,timeComparisonS},null,null,null);
	   
	   int totalLength=cursor1.getCount()+cursor2.getCount()+cursor3.getCount();
	   
	   int returnValues[][]=new int[totalLength][2];
	   int arrayCounter=0;
	   
	   for(int i=0;i<cursor1.getCount();i++)
	   {
		   cursor1.moveToNext();
		   returnValues[arrayCounter][0]=cursor1.getInt(0);
		   returnValues[arrayCounter][1]=0;
		   arrayCounter++;
	   }
	   
	   for(int i=0;i<cursor2.getCount();i++)
	   {
		   cursor2.moveToNext();
		   returnValues[arrayCounter][0]=cursor2.getInt(0);
		   returnValues[arrayCounter][1]=1;
		   arrayCounter++;
	   }
	   
	   for(int i=0;i<cursor3.getCount();i++)
	   {
		   cursor3.moveToNext();
		   returnValues[arrayCounter][0]=cursor3.getInt(0);
		   returnValues[arrayCounter][1]=2;
		   arrayCounter++;
	   }
	   
	   cursor1.close();
	   cursor2.close();
	   cursor3.close();
	   
	   return returnValues;
   }
   /**
    * Adds all the start categories from the XML
    */
   public void addCategories()
   {
	   
	   try
	   {
		   String[] categoryNames = context.getResources().getStringArray(R.array.category_names);
		   String[] categoryUrls = context.getResources().getStringArray(R.array.catergory_rss_urls);
		   for(int i=0;i<categoryNames.length;i++)
		   {
			   insertCategory(categoryNames[i],true,categoryUrls[i]);
		   } 
	   }catch(NullPointerException e)
	   {
		   Log.e("categories-xml","Categories XML is broken");
	   }
   }
   /**
    * Checks whether there are any records in category
    * @return true or false
    */
   public boolean isCreated()
   {
	   try
	   {
		   getCategoryBooleans()[0]=true;
		   return true;
	   }
	   catch(Exception e)
	   {
		   return false;
	   }
   }
   /**
    * Inserts a category into the category table.
    * @param name Name of the category as String
    * @param enabled Whether the RSSFeed should be fetched as Boolean
    */
   public void insertCategory(String name,boolean enabledB,String url)
   {
	   int enabledI;
	   if(enabledB){enabledI=1;}else{enabledI=0;}
	   ContentValues cv=new ContentValues(3);
	   cv.put("name",name);
	   cv.put("enabled",enabledI);
	   cv.put("url",url);
	   db.insert(CATEGORY_TABLE, null, cv);
   }
   /**
    * Clears all the tables in the database, leaving structure intact.
    */
   public void clear() {
      db.execSQL("DELETE from "+ITEM_TABLE);
      db.execSQL("DELETE from "+CATEGORY_TABLE);
      db.execSQL("DELETE from "+ITEM_CATEGORY_TABLE);
      }
   /**
    * Drops the entire database then rebuilds it.
    */
   public void dropTables()
   {
	  db.execSQL("DROP TABLE "+ITEM_TABLE);
	  db.execSQL("DROP TABLE "+CATEGORY_TABLE);
	  db.execSQL("DROP TABLE "+ITEM_CATEGORY_TABLE);
   }
   /**
    * Attempts to create the tables.
    */
   public void createTables()
   {
	   db.execSQL(TABLE_CREATE);
	   db.execSQL(TABLE2_CREATE);
	   db.execSQL(TABLE3_CREATE);
   }
   /**
    * Drops then recreates all the tables.
    */
   public void reset()
   {
	   dropTables();
	   createTables();
   }
   /**
    * Queries the categories table for the enabled column of all rows,
    * returning an array of booleans representing whether categories are enabled or not,
    * sorted by category_Id.
    * @return boolean[] containing enabled column from categories table.
    */
   public boolean[] getCategoryBooleans()
   {
	   //FIXME Optimise
	   Cursor cursor=db.query(CATEGORY_TABLE, new String[]{"enabled"}, null, null, null, null, "category_Id");
	   boolean[] enabledCategories = new boolean[cursor.getCount()];
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   if(cursor.getInt(0)==0)
		   {
			   enabledCategories[i-1]=false;
		   }
		   else
		   {
			   enabledCategories[i-1]=true;
		   }
	   }
	   cursor.close();
	return enabledCategories;
   }
   /**
    * Returns the links and names of all the categories that are enabled.
    * @return A string[][] containing the String urls in [0] and String names in [1].
    */
   public String[][] getEnabledCategories()
   {
	   //Queries the category table to get a list of enabled categories
	   Cursor cursor=db.query(CATEGORY_TABLE, new String[]{"url"}, "enabled='1'", null, null, null, "category_Id");
	   Cursor cursor2=db.query(CATEGORY_TABLE, new String[]{"name"}, "enabled='1'", null, null, null, "category_Id");
	   String[][] categories=new String[2][cursor.getCount()];
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   cursor2.moveToNext();
		   categories[0][i-1]=cursor.getString(0);
		   categories[1][i-1]=cursor2.getString(0);
	   }
	   cursor.close();
	   cursor2.close();
	   return categories;
   }
   /**
    * Takes an array of booleans and sets the first n categories
    * to those values. Where n is length of array
    * @param enabled A boolean array of "enabled" values
    */
   public void setEnabledCategories(boolean[] enabled) throws NullPointerException
   {
	   ContentValues cv=new ContentValues(1);
	   for(int i=1;i<enabled.length;i++)
	   {
		   if(enabled[i]){cv.put("enabled", 1);}
		   else{cv.put("enabled", 0);}
		   db.update(CATEGORY_TABLE, cv, "category_Id=?", new String[]{Integer.toString(i)});
		   cv.clear();
	   }
   }
   /**
    * Takes a category and returns all the title, description ,link and item_Id of all
    * the items related to it.
    * Returns null if no items exists
    * @param category The Case-sensitive name of the category
    * @return A String[{title,description,link,item_Id}][{item1,item2}].
    */
   public String[][] getItems(String category)
   {
	   //FIXME Optimise, add limit? NOT SQL INJECTION SAFE (But internal, so k)
	   try{
	   //Query the relation table to get a list of Item_Ids.
	   Cursor cursor=db.query(ITEM_CATEGORY_TABLE, new String[]{"itemId"}, "categoryName=?", new String[]{category}, null, null, null);
	   /*Create a string consisting of the first item_Id, then a loop appending
	    * ORs and further item_Id
	   */
	   cursor.moveToNext();
	   String itemIdQuery=new String("item_Id='"+cursor.getString(0)+"'");
	   for(int i=2;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   itemIdQuery+=(" OR item_Id='"+cursor.getString(0)+"'");
	   }
	   //Query the items table to get a the rows with that category
	   //then fill the String[][] and return it
	   cursor=db.query(ITEM_TABLE,new String[]{"title", "description", "link", "item_Id"},itemIdQuery,null,null,null,"pubdate desc");
	   String[][] items=new String[4][cursor.getCount()];
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   items[0][i-1]=cursor.getString(0);
		   items[1][i-1]=cursor.getString(1);
		   items[2][i-1]=cursor.getString(2);
		   items[3][i-1]=cursor.getString(3);
	   }
	   cursor.close();
	   return items;}
	   catch(Exception e)
	   {
		   Log.i("Database","Tried to get items from an empty table (Items)");
		   return null;
	   }
   }
   /**
    * Sets the given category to the given boolean
    * @param category The String category you wish to change.
    * @param enabled The boolean value you wish to set it to.
    */
   public void setEnabled(String category,boolean enabled)
   {
	   //FIXME Skip first step?
	   //Query the categories table for the id of the category with that name
	   //Then fetch the id from the first one returned
	   Cursor cursor=db.query(CATEGORY_TABLE,new String[]{"category_Id"},"name=?",new String[]{category},null,null,null);
	   cursor.moveToNext();
	   int categoryId=cursor.getInt(0);
	   //Create a box containing the new value/column
	   ContentValues cv=new ContentValues(1);
	   if(enabled){cv.put("enabled", 1);}
	   else{cv.put("enabled", 0);}
	   //push up to database.
	   db.update(CATEGORY_TABLE, cv, "category_Id=?", new String[]{Integer.toString(categoryId)});
	   cursor.close();
   }
   /**
    * When called will remove all articles that are
    * over one month to the second old. Then cleans up
    * the relationship table. Possibly resource intensive.
    */
   public void clearOld()
   {
	   
	   //FIXME Add parameter, customize the date it wipes from. Optimise?
	   //Creates a java.util date object with current time
	   //Subtracts one month in milliseconds and deletes all
	   //items with a pubdate less than that value.
	   Date now=new Date();
	   long oldTime=(now.getTime()-2629743000L);
	   Cursor cursor=db.query(ITEM_TABLE,new String[]{"item_Id"},"pubdate<?",new String[]{Long.toString(oldTime)},null,null,null);
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   db.delete(ITEM_CATEGORY_TABLE,"itemId=?",new String[]{Integer.toString(cursor.getInt(0))});
	   }
	   db.delete(ITEM_TABLE,"pubdate<?",new String[]{Long.toString(oldTime)});
	   cursor.close();
   }
   public void shutdown()
   {
	   db.close();
   }
   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
    	  //Creates the three tables
    	  
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + ITEM_TABLE);
         onCreate(db);
      }
   }
}
