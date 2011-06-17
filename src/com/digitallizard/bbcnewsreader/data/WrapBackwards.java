package com.digitallizard.bbcnewsreader.data;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class WrapBackwards {
	public static long insertWithOnConflict(SQLiteDatabase db, String table, String nullColumnHack, ContentValues values, int conflictAlgorithm){
		return db.insertWithOnConflict(table, nullColumnHack, values, conflictAlgorithm);
	}
}
