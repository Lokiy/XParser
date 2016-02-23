/**
 * Copyright (C) 2014 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lokiy.x.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.lokiy.x.db.util.DBUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Simple DBHelper.
 *
 * @author Luki
 */
/*public*/class XDBHelper implements DBHelper {

	private SQLiteDatabase db;
	private String dbName;
	private DBUtils dbUtils;

	XDBHelper(String dbName, Context context) {
		check(context);
		db = new SQLHelper(context.getApplicationContext(), this.dbName = dbName, null, 1).getWritableDatabase();
		dbUtils = DBUtils.getInstance(db, dbName, this);
	}

	/**
	 * check context
	 *
	 * @param context context
	 */
	private void check(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("XDBHelper context can't be null");
		}
	}

	/**
	 * Convenience method for inserting a row into the database.
	 *
	 * @param t save data fro inserting
	 * @return the row ID of the newly inserted row, or -1 if an error occurred or exist
	 */
	public <T extends Serializable> long insert(T t) {
		return dbUtils.insert(t);
	}

	/**
	 * Convenience method for updating rows in the database.
	 *
	 * @param t data list for updating
	 * @return the number of rows affected
	 */
	public <T extends Serializable> int update(T t) {
		return dbUtils.update(t);
	}

	/**
	 * Convenience method for updating or inserting rows in the database.
	 *
	 * @param bean updating or inserting data
	 * @return the number of rows affected
	 */
	public <T extends Serializable> int save(T bean) {
		return dbUtils.save(bean);
	}

	/**
	 * Convenience method for updating or inserting rows in the database.
	 *
	 * @param list save data list for updating or inserting
	 * @return the number of rows affected
	 */
	public <T extends Serializable> int save(List<T> list) {
		return dbUtils.save(list);
	}

	/**
	 * Convenience method for deleting rows in the database.
	 *
	 * @param list data list for deleting.
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a
	 * count pass "1" as the whereClause.
	 */
	public <T extends Serializable> int delete(List<T> list) {
		return dbUtils.delete(list);
	}

	/**
	 * Convenience method for deleting rows in the database.
	 *
	 * @param t data for deleting.
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a
	 * count pass "1" as the whereClause.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> int delete(T t) {
		return dbUtils.delete(t);
	}

	/**
	 * Convenience method for deleting rows in the database.
	 *
	 * @param clazz data for deleting.
	 * @return the number of rows affected if a whereClause is passed in, 0 otherwise. To remove all rows and get a
	 * count pass "1" as the whereClause.
	 */
	public <T extends Serializable> int deleteBySelection(Class<T> clazz, DBSelection<T> selection) {
		return dbUtils.deleteBySelection(clazz, selection);
	}

	/**
	 * find the data with bean.
	 *
	 * @param bean which contains field' value. And that can auto consist of selection.
	 * @return T
	 */
	public <T extends Serializable> T findByBean(T bean) {
		return dbUtils.findByBean(bean);
	}

	/**
	 * find the data with selection.
	 *
	 * @param clazz     table and bean.
	 * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE
	 *                  itself). Passing null will return all rows for the given table.
	 * @return clazz's instance
	 */
	public <T extends Serializable> T findBySelection(Class<T> clazz, DBSelection<T> selection) {
		return dbUtils.findBySelection(clazz, selection);
	}

	/**
	 * find the data with bean.
	 *
	 * @param bean which contains field' value. And that can auto consist of selection.
	 * @return List
	 */
	public <T extends Serializable> List<T> selectByBean(T bean) {
		return dbUtils.selectByBean(bean);
	}

	/**
	 * find the data with selection.
	 *
	 * @param clazz     table and bean.
	 * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE
	 *                  itself). Passing null will return all rows for the given table.
	 * @return clazz's instance
	 */
	public <T extends Serializable> List<T> selectBySelection(Class<T> clazz, DBSelection<T> selection) {
		return dbUtils.selectBySelection(clazz, selection);
	}

	public synchronized void close() {
		if (db != null) {
			db.close();
		}
		DBEntryMap.destroy(dbName);
	}

	public boolean isOpen() {
		return db.isOpen();
	}
}
