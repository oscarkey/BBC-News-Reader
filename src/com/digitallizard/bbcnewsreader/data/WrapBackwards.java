/*******************************************************************************
 * BBC News Reader
 * Released under the BSD License. See README or LICENSE.
 * Copyright (c) 2011, Digital Lizard (Oscar Key, Thomas Boby)
 * All rights reserved.
 ******************************************************************************/
package com.digitallizard.bbcnewsreader.data;

import android.content.ContentValues;

public class WrapBackwards {
	public static long insertWithOnConflict(DatabaseHelper db, String table, ContentValues values, int conflictAlgorithm) {
		return db.insertWithOnConflict(table, values, conflictAlgorithm);
	}
}
