package com.digitallizard.bbcnewsreader.data;

import android.content.ContentValues;

public class WrapBackwards {
	public static long insertWithOnConflict(DatabaseHelper db, String table, ContentValues values, int conflictAlgorithm){
		return db.insertWithOnConflict(table, values, conflictAlgorithm);
	}
}
