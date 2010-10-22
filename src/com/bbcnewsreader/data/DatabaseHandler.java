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
   private static final String InsertItem = "insert into " + TABLE_NAME + "(title, description, link, pubdate) values(?,?,?,?)";

   public DatabaseHandler(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
   }

   public long insert(String name) {
      this.insertStmt.bindString(1, name);
      return this.insertStmt.executeInsert();
   }
   public void insertItem(String title, String description, String link, String pubdate)
   {
	   /*this.insertStmt=this.db.compileStatement(InsertItem);
	   this.insertStmt.bindString(1, title);
	   this.insertStmt.bindString(2, description);
	   this.insertStmt.bindString(3, link);
	   this.insertStmt.bindString(4, pubdate);
	   this.insertStmt.executeInsert();*/
	   Log.v("ERROR","insert into " + TABLE_NAME + " (item_Id, title, description, link, pubdate) values (null, "+title+", "+description+", "+link+", "+pubdate+")");
	   //this.insertStmt=this.db.compileStatement("insert into " + TABLE_NAME + " (item_Id, title, description, link, pubdate) values (null, '"+title+"', '"+description+"', '"+link+"', '"+pubdate+"')");
	   this.insertStmt=this.db.compileStatement("insert into " + TABLE_NAME + " values (null, '"+title+"', '"+description+"', '"+link+"', '"+pubdate+"')");
	   this.insertStmt.executeInsert();
   }

   public void deleteAll() {
      this.db.delete(TABLE_NAME, null, null);
   }

   public List<String> selectAll() {
      List<String> list = new ArrayList<String>();
      Cursor cursor = this.db.query(TABLE_NAME, new String[] { "name" }, 
        null, null, null, null, "name desc");
      if (cursor.moveToFirst()) {
         do {
            list.add(cursor.getString(0)); 
         } while (cursor.moveToNext());
      }
      if (cursor != null && !cursor.isClosed()) {
         cursor.close();
      }
      return list;
   }

   private static class OpenHelper extends SQLiteOpenHelper {

      OpenHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
    	  //Item table
    	  db.delete(TABLE_NAME, null,null);
         db.execSQL("CREATE TABLE " + TABLE_NAME + 
          "(item_Id integer PRIMARY KEY," +
          "title varchar(255), " +
          "description varchar(255), " +
          "link varchar(255), " +
          "pubdate varchar(255))");
         //Category table
         db.execSQL("CREATE TABLE " + TABLE2_NAME +
          "(category_Id integer PRIMARY KEY," +
          "name varchar(255))");
         //Link table
         db.execSQL("CREATE TABLE " + TABLE3_NAME +
          "(categoryId INT, " +
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
