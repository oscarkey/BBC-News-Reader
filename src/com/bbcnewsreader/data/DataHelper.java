package com.bbcnewsreader.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataHelper {

   private static final String DATABASE_NAME = "bbcnewsreader.db";
   private static final int DATABASE_VERSION = 1;
   private static final String TABLE_NAME = "items";
   private static final String TABLE2_NAME = "categories";
   private static final String TABLE3_NAME = "categories_items";

   private Context context;
   private SQLiteDatabase db;

   private SQLiteStatement insertStmt;
   private static final String INSERT = "insert into " 
      + TABLE_NAME + "(name) values (?)";

   public DataHelper(Context context) {
      this.context = context;
      OpenHelper openHelper = new OpenHelper(this.context);
      this.db = openHelper.getWritableDatabase();
      this.insertStmt = this.db.compileStatement(INSERT);
   }

   public long insert(String name) {
      this.insertStmt.bindString(1, name);
      return this.insertStmt.executeInsert();
   }
   public void insertItem(String title, String description, String link, String pubdate)
   {
	   
	   
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
         db.execSQL("CREATE TABLE " + TABLE_NAME + 
          "(P_Id int NOT NULL AUTO_INCREMENT," +
          "title varchar(255), " +
          "description varchar(255)" +
          "link varchar(255)" +
          "pubdate varchar(255))");
         //Category table
         db.execSQL("CREATE TABLE" + TABLE2_NAME +
          "P_Id int NOT NULL AUTO_INCREMENT," +
          "name varchar(255)");
         //Link table
         db.execSQL("CREATE TABLE" + TABLE3_NAME +
          "categoryId INT NOT NULL" +
          "itemId INT NOT NULL");
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w("Example", "Upgrading database, this will drop tables and recreate.");
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
         onCreate(db);
      }
   }
}
