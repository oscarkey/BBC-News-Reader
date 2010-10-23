package com.bbcnewsreader.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler {

   private static final String DATABASE_NAME = "bbcnewsreader.db";
   private static final int DATABASE_VERSION = 1;
   private static final String TABLE_NAME = "items";
   private static final String TABLE2_NAME = "categories";
   private static final String TABLE3_NAME = "categories_items";
   private static final String TABLE_CREATE ="CREATE TABLE " + TABLE_NAME + 
   											  "(item_Id integer PRIMARY KEY," +
									          "title varchar(255), " +
									          "description varchar(255), " +
									          "link varchar(255), " +
									          "pubdate varchar(255))";
   private static final String TABLE2_CREATE="CREATE TABLE " + TABLE2_NAME +
									          "(category_Id integer PRIMARY KEY," +
									          "name varchar(255)," +
									          "enabled int," +
									          "url varchar(255))";
   private static final String TABLE3_CREATE="CREATE TABLE " + TABLE3_NAME +
									          "(categoryName varchar(255), " +
									          "itemId INT)";

   private Context context;
   private SQLiteDatabase db;

   private SQLiteStatement insertStmt;
   public DatabaseHandler(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
   }
   /**
    * Inserts an RSSItem into the items table, then creates an entry in the relationship
    * table between it and its category
    * @param title News item's Title as String
    * @param description News item's Description as String
    * @param link News item's link as String
    * @param pubdate News item's published data as String
    * @param category News item's category as String
    */
   public void insertItem(String title, String description, String link, String pubdate, String category)
   {
	   //Compiles then executes the insertion of the item into the items database.
	   //Takes the rowid of the new record and uses it to get the item_id.
	   //Moves to first item in Cursor then inserts item_id and category into relationship table.
	   this.insertStmt=this.db.compileStatement("insert into " + TABLE_NAME + " values (NULL, '"+title+"', '"+description+"', '"+link+"', '"+pubdate+"')");
	   long rowid=this.insertStmt.executeInsert();
	   Cursor cursor=db.query(false,"items",new String[]{"item_Id"},("rowid='"+rowid+"'"),null,null,null,null, null);
	   cursor.moveToNext();
	   int itemid=cursor.getInt(0);
	   this.insertStmt=this.db.compileStatement("insert into " + TABLE3_NAME + " values ('"+category+"', '"+itemid+"')");
	   this.insertStmt.executeInsert();
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
	   this.insertStmt=this.db.compileStatement("insert into " +TABLE2_NAME + " values (NULL, '"+name+"', '"+enabledI+"', '"+url+"')");
	   this.insertStmt.executeInsert();
   }
   /**
    * Clears all the tables in the database, leaving structure intact.
    */
   public void clear() {
      db.execSQL("DELETE from "+TABLE_NAME);
      db.execSQL("DELETE from "+TABLE2_NAME);
      db.execSQL("DELETE from "+TABLE3_NAME);
      }
   /**
    * Drops the entire database then rebuilds it.
    */
   public void dropTables()
   {
	  db.execSQL("DROP TABLE "+TABLE_NAME);
	  db.execSQL("DROP TABLE "+TABLE2_NAME);
	  db.execSQL("DROP TABLE "+TABLE3_NAME);
	  db.execSQL(TABLE_CREATE);
	  db.execSQL(TABLE2_CREATE);
	  db.execSQL(TABLE3_CREATE);
   }
   /**
    * Queries the categories table for the enabled column of all rows,
    * returning an array of booleans representing whether categories are enabled or not,
    * sorted by category_Id.
    * @return boolean[] containing enabled column from categories table.
    */
   public boolean[] getEnabledCategories()
   {
	   //FIXME Optimise
	   Cursor cursor=db.query(TABLE2_NAME, new String[]{"enabled"}, null, null, null, null, "category_Id");
	   boolean[] enabledCategories = new boolean[cursor.getCount()];
	   Log.v("TEST",cursor.toString());
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
	return enabledCategories;
   }
   /**
    * Takes a category and returns all the title, description and lnik of all
    * the items related to it.
    * @param category The Case-sensitive name of the category
    * @return A String[{title,description,link}][{item1,item2}].
    */
   public String[][] getItems(String category)
   {
	   //FIXME Optimise
	   //Query the relation table to get a list of Item_Ids.
	   Cursor cursor=db.query(TABLE3_NAME, new String[]{"itemId"}, "categoryName='"+category+"'", null, null, null, null);
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
	   cursor=db.query(TABLE_NAME,new String[]{"title", "description", "link"},itemIdQuery,null,null,null,"pubdate");
	   String[][] items=new String[3][cursor.getCount()];
	   for(int i=1;i<=cursor.getCount();i++)
	   {
		   cursor.moveToNext();
		   items[0][i-1]=cursor.getString(0);
		   items[1][i-1]=cursor.getString(1);
		   items[2][i-1]=cursor.getString(2);
	   }
	   return items;
   }
   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
    	  //Creates the three tables
    	  db.execSQL(TABLE_CREATE);
    	  db.execSQL(TABLE2_CREATE);
    	  db.execSQL(TABLE3_CREATE);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}
