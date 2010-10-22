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
   public void insertCategory(String name,String enabled)
   {
	   this.insertStmt=this.db.compileStatement("insert into " +TABLE2_NAME + " values (NULL, '"+name+"', '"+enabled+"')");
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

   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
    	  //Item table
         db.execSQL("CREATE TABLE " + TABLE_NAME + 
          "(item_Id integer PRIMARY KEY," +
          "title varchar(255), " +
          "description varchar(255), " +
          "link varchar(255), " +
          "pubdate varchar(255))");
         //Category table
         db.execSQL("CREATE TABLE " + TABLE2_NAME +
          "(category_Id integer PRIMARY KEY," +
          "name varchar(255)," +
          "enabled boolean)");
         //Link table
         db.execSQL("CREATE TABLE " + TABLE3_NAME +
          "(categoryName varchar(255), " +
          "itemId INT)");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}
