/**
 * Copyright (C) 2014 Luki(liulongke@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package luki.x.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import luki.x.base.IDBHelper;
import luki.x.base.XLog;
import luki.x.db.DBSelection;
import luki.x.db.Table;
import luki.x.db.TableInfo;
import luki.x.db.annotation.Encrypt;
import luki.x.db.annotation.Expose;
import luki.x.db.annotation.NotNull;
import luki.x.db.annotation.OrderBy;
import luki.x.db.annotation.OrderBy.SortAs;
import luki.x.db.annotation.Rename;
import luki.x.db.annotation.TableVersion;
import luki.x.db.annotation.Unique;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class DBUtils {

	private final String TAG = "XDBHelper";
	public static final String TABLE_INFO = "table_info";
	public static final String PRIMARYKEY_COLUMN = "_id";
	public static final String TIME_COLUMN = "_modify_time";

	@SuppressWarnings("rawtypes") public static Map<String, Table> tableMap = new HashMap<String, Table>();
	private static final Map<String, DBUtils> instanceMap = new HashMap<String, DBUtils>();
	private SQLiteDatabase db;
	private String dbName;
	private IDBHelper helper;

	private DBUtils(SQLiteDatabase db, String dbName, IDBHelper helper) {
		this.db = db;
		this.dbName = dbName;
		this.helper = helper;
	}

	public static DBUtils getIntance(SQLiteDatabase db, String dbName, IDBHelper helper) {
		DBUtils dbUtils;
		if ((dbUtils = instanceMap.get(dbName)) == null) {
			dbUtils = new DBUtils(db, dbName, helper);
			instanceMap.put(dbName, dbUtils);
		}
		return dbUtils;
	}

	/**
	 * Unique Selection
	 * 
	 * @param bean
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public <T extends Serializable> DBSelection<T> getUniqueSelection(Class<T> clazz) {
		DBSelection<T> dbSelection = new DBSelection<T>();
		StringBuffer selection = new StringBuffer();
		Field[] fields = clazz.getDeclaredFields();
		List<Field> l = new ArrayList<Field>();
		for (Field f : fields) {
			f.setAccessible(true);
			if (isNotExcept(f) && f.getAnnotation(Unique.class) != null) {
				String name = f.getName();
				selection.append("`");
				selection.append(name);
				selection.append("`");
				selection.append("=?");
				selection.append(" AND");
				l.add(f);
			}
		}
		if (selection.length() > 4) {
			selection.delete(selection.length() - 4, selection.length());
		}
		dbSelection.selection = selection.toString();
		dbSelection.setUniqueSelections(l);
		return dbSelection;
	}

	/**
	 * ContentValues
	 * 
	 * @param bean
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public <T extends Serializable> DBSelection<T> getSelection(T bean) {
		DBSelection<T> dbSelection = new DBSelection<T>();
		StringBuffer selection = new StringBuffer();
		StringBuffer orderBy = new StringBuffer();
		List<Field> orderByList = new ArrayList<Field>();
		List<String> values = new ArrayList<String>();
		Field[] fields = bean.getClass().getDeclaredFields();
		boolean isSort = false;
		for (Field f : fields) {
			f.setAccessible(true);
			if (isNotExcept(f)) {
				Object value = null;
				try {
					value = f.get(bean);
				} catch (Exception e) {
					XLog.w(TAG, e);
				}
				String name = f.getName();
				if (((value instanceof Number && ((Number) value).intValue() != 0) || value instanceof String || value instanceof Byte[])) {
					selection.append("`");
					selection.append(name);
					selection.append("`");
					selection.append("=?");
					selection.append(" AND ");
					values.add(String.valueOf(value));
				} else if (value != null) {
					// 不是标准数据类型抛弃 或者生成联查表Sql?
				}
				OrderBy ob = f.getAnnotation(OrderBy.class);
				if (ob != null) {
					orderByList.add(f);
					if (ob.orderPosition() != 0) {
						isSort = true;
					}
				}
			}
		}
		if (selection.length() > 4) {
			selection.delete(selection.length() - 4, selection.length());
		}
		String[] selectionArgs = new String[values.size()];
		values.toArray(selectionArgs);
		if (orderByList.size() > 0) {
			Field[] fieldArray = new Field[orderByList.size()];
			orderByList.toArray(fieldArray);
			for (int i = 0; i < fieldArray.length - 1 && isSort; i++) { // 最多做n-1趟排序
				for (int j = 0; j < fieldArray.length - i - 1; j++) { // 对当前无序区间score[0......length-i-1]进行排序(j的范围很关键，这个范围是在逐步缩小的)
					int p1 = fieldArray[j].getAnnotation(OrderBy.class).orderPosition();
					int p2 = fieldArray[j + 1].getAnnotation(OrderBy.class).orderPosition();
					if (p1 > p2) { // 把小的值交换到后面
						Field temp = fieldArray[j];
						fieldArray[j] = fieldArray[j + 1];
						fieldArray[j + 1] = temp;
					}
				}
			}
			for (Field field : fieldArray) {
				orderBy.append("`");
				orderBy.append(field.getName());
				orderBy.append("`");
				orderBy.append(field.getAnnotation(OrderBy.class).value() == SortAs.ASC ? " ASC" : " DESC");
				orderBy.append(",");
			}
		}
		if (orderBy.length() > 1) {
			orderBy.deleteCharAt(orderBy.length() - 1);
		}
		dbSelection.selection = selection.length() > 0 ? selection.toString() : null;
		dbSelection.selectionArgs = selection.length() > 0 ? selectionArgs : null;
		dbSelection.orderBy = orderBy.length() > 0 ? orderBy.toString() : null;
		return dbSelection;
	}

	private boolean isNotExcept(Field f) {
		return !Modifier.isTransient(f.getModifiers()) && !Modifier.isStatic(f.getModifiers()) && f.getAnnotation(Expose.class) == null;
	}

	/**
	 * 得到插入表的ContentValues
	 * 
	 * @param bean
	 * @return
	 * @throws Exception
	 */
	public <T extends Serializable> ContentValues getContentValues(T bean) throws Exception {
		ContentValues values = new ContentValues();
		boolean hasUnique = false;
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			if (isNotExcept(f)) {
				if (f.getAnnotation(Unique.class) != null) {
					hasUnique = true;
				}
				Object value = null;
				try {
					value = f.get(bean);
				} catch (Exception e) {
					XLog.w(TAG, e);
				}
				String name = f.getName();
				if (f.getAnnotation(Encrypt.class) != null && value != null && ReflectUtils.isNormalGenericType(value.getClass())) {
					values.put(name, DESUtil.encrypt(value.toString(), name));
				} else if (value instanceof Long) {
					values.put(name, (Long) value);
				} else if (value instanceof String) {
					values.put(name, (String) value);
				} else if (value instanceof Double) {
					values.put(name, (Double) value);
				} else if (value instanceof Integer) {
					values.put(name, (Integer) value);
				} else if (value instanceof Float) {
					values.put(name, (Float) value);
				} else if (value instanceof Short) {
					values.put(name, (Short) value);
				} else if (value instanceof Byte) {
					values.put(name, (Byte) value);
				} else if (value instanceof Boolean) {
					values.put(name, (Boolean) value);
				} else if (value instanceof Byte[]) {
					values.put(name, (byte[]) value);
				} else {
					// 不是标准数据类型 联查表设置值还是抛弃？ 暂时在插入和更新的时候自己联查
				}
			}
		}
		if (!hasUnique) {
			values.put(PRIMARYKEY_COLUMN, (String) null);
		}
		values.put(TIME_COLUMN, System.currentTimeMillis());
		return values;
	}

	/**
	 * create the table with the class.
	 * 
	 * @param clazz
	 * @param table
	 * @return
	 */
	public <T extends Serializable> void createTable(Class<T> clazz) {
		db.execSQL(getCreateTableSQL(clazz, null));
	}

	/**
	 * 获取创建表的语句
	 * 
	 * @param clazz
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends Serializable> String getCreateTableSQL(Class<T> clazz, String tableName) {
		if (TextUtils.isEmpty(tableName)) {
			tableName = clazz.getSimpleName();
		}
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS " + tableName + "(");
		Field[] fields = clazz.getDeclaredFields();
		sql.append("`");
		sql.append(PRIMARYKEY_COLUMN);
		sql.append("`");
		sql.append(" INTEGER PRIMARY KEY AUTOINCREMENT ");
		sql.append(",");
		for (Field field : fields) {
			field.setAccessible(true);
			if (isNotExcept(field)) {
				NotNull c = field.getAnnotation(NotNull.class);
				Type type = field.getGenericType();
				if (!ReflectUtils.isNormalGenericType(type)) {
					sql.append("`");
					sql.append(field.getName());
					sql.append("`");
					Class<T> clazz1 = null;
					if ((field.getType() == List.class || field.getType() == ArrayList.class) && type instanceof ParameterizedType) {
						clazz1 = (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[0];
					} else if (type instanceof Class) {
						clazz1 = (Class<T>) type;
					}
					if (clazz1 != null && !ReflectUtils.isNormalGenericType(clazz1)) {
						checkTable(clazz1);
					}
				} else {
					sql.append("`");
					sql.append(field.getName());
					sql.append("`");
				}
				sql.append(" TEXT");
				if (c != null) {
					sql.append(" NOT NULL ");
				}
				sql.append(",");
			}
		}
		sql.append("`");
		sql.append(TIME_COLUMN);
		sql.append("`");
		sql.append(" TEXT");
		sql.append(");");
		XLog.v(TAG, sql.toString());
		return sql.toString();
	}

	/**
	 * 根据游标给一个Object 设置属性
	 * 
	 * @param bean
	 * @param c
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public <T extends Serializable> void setObject(T bean, Cursor c) throws Exception {
		Field[] fields = bean.getClass().getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			String columnName = f.getName();
			int columnIndex = c.getColumnIndex(columnName);
			if (columnIndex == -1) {
				continue;
			}
			if (ReflectUtils.isNormalGenericType(f.getGenericType())) {
				String columnValue = c.getString(columnIndex);
				Object v = null;
				if (f.getAnnotation(Encrypt.class) != null) {
					columnValue = DESUtil.decrypt(columnValue, columnName);
				}
				if (f.getGenericType() == Long.class || f.getGenericType() == long.class) {
					v = Long.valueOf(columnValue);
				} else if (f.getGenericType() == String.class) {
					v = String.valueOf(columnValue);
				} else if (f.getGenericType() == Double.class || f.getGenericType() == double.class) {
					v = Double.valueOf(columnValue);
				} else if (f.getGenericType() == Integer.class || f.getGenericType() == int.class) {
					v = Integer.valueOf(columnValue);
				} else if (f.getGenericType() == Float.class || f.getGenericType() == float.class) {
					v = Float.valueOf(columnValue);
				} else if (f.getGenericType() == Short.class || f.getGenericType() == short.class) {
					v = Short.valueOf(columnValue);
				} else if (f.getGenericType() == Boolean.class || f.getGenericType() == boolean.class) {
					v = Boolean.valueOf(columnValue);
				} else if (f.getGenericType() == Byte[].class || f.getGenericType() == byte[].class) {
					v = c.getBlob(columnIndex);
				}
				f.set(bean, v);
			} else {

			}
		}
	}

	/**
	 * Creates a new instance of the class represented base on the {@link Cursor} by this Class object. The class is
	 * instantiated as if by a new
	 * expression with an empty argument list. The class is initialized if it has not already been initialized.
	 * 
	 * @param clazz
	 * @param c
	 * @return
	 */
	public <T extends Serializable> T getObject(Class<T> clazz, Cursor c) throws Exception {
		T t = (T) clazz.newInstance();
		setObject(t, c);
		return t;
	}

	/**
	 * check table is exist.
	 * 
	 * @param clazz
	 * @return the table instance.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <T extends Serializable> Table<T> checkTable(Class<T> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("XDBHelper when create or check table , the class can't be null.");
		}
		String tableName = clazz.getSimpleName();
		String key = dbName + "_" + tableName;
		Table<T> table = tableMap.get(key);
		if (table == null) {
			table = new Table<T>();
			table.tableName = tableName;
			table.tableClass = clazz;
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				if (!ReflectUtils.isNormalGenericType(field.getGenericType()) && isNotExcept(field)) {
					table.otherTypeField.add(field);
				}
			}
			tableMap.put(key, table);
		} else
			return table;
		Table<TableInfo> tableInfo = (Table<TableInfo>) checkTable(TableInfo.class);
		Cursor cursor = null;
		try {
			table.uniqueSelection = getUniqueSelection(clazz);
			String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='" + table.tableName + "' ";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.moveToNext()) {
				int count = cursor.getInt(0);
				if (count > 0) {
					table.isExist = true;
				}
			}
			TableInfo info = new TableInfo();
			info.tableClass = clazz.getName();
			info.tableName = clazz.getSimpleName();
			if (!table.isExist) {
				createTable(clazz);
				db.insert(tableInfo.tableName, null, getContentValues(info));
				table.isExist = true;
			} else {
				TableVersion tableVersion = clazz.getAnnotation(TableVersion.class);
				int version = tableVersion == null ? 1 : tableVersion.value();
				int dbVersion = 1;
				TableInfo result = helper.findByBean(info);
				if (result != null) {
					dbVersion = result.tableVersion;
				}
				if (version > dbVersion) {
					XLog.start(TAG, "MODIFY TABLE" + info.tableName);
					db.beginTransaction();
					try {
						checkFieldInTable(clazz);
						db.execSQL("UPDATE " + tableInfo.tableName + " SET tableVersion = " + version + " WHERE tableName = '"
								+ table.tableName + "'");
						db.setTransactionSuccessful();
					} catch (Exception e) {
						XLog.w(TAG, e);
					}
					db.endTransaction();
					XLog.end(TAG, "MODIFY TABLE" + info.tableName);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) cursor.close();
			cursor = null;
		}
		return table;
	}

	public <T extends Serializable> void checkFieldInTable(Class<T> clazz) {
		String tableName = clazz.getSimpleName();
		String sql = "SELECT * FROM " + tableName + " WHERE 1 != 1";
		Cursor c = db.rawQuery(sql, null);
		if (c == null) {
			return;
		}
		Field[] fields = clazz.getDeclaredFields();
		List<String> deleteColumns = new ArrayList<String>();
		List<String> addColumns = new ArrayList<String>();
		Map<String, List<String>> renameColumns = new HashMap<String, List<String>>();
		StringBuffer newColumns = new StringBuffer(PRIMARYKEY_COLUMN + "," + TIME_COLUMN);
		StringBuffer orignalColumns = new StringBuffer(PRIMARYKEY_COLUMN + "," + TIME_COLUMN);
		for (Field f : fields) {
			f.setAccessible(true);
			if (!isNotExcept(f)) {
				continue;
			}
			String columnName = f.getName();
			Rename r = f.getAnnotation(Rename.class);
			String[] names = r == null ? new String[] {} : dealEmptyValue(r.value());
			if (names.length > 0) {
				renameColumns.put(columnName, Arrays.asList(names));
			}
			boolean isExist = c.getColumnIndex(columnName) > 0;
			if (isExist) {// exist column. no change
				newColumns.append(",");
				newColumns.append("`");
				newColumns.append(columnName);
				newColumns.append("`");
				orignalColumns.append(",");
				orignalColumns.append("`");
				orignalColumns.append(columnName);
				orignalColumns.append("`");
			} else { // not exist. delete or rename or add
				if (r != null && names.length > 0) { // rename.
					for (String orginalColumn : names) {
						isExist = c.getColumnIndex(orginalColumn) > 0;
						if (isExist) {
							newColumns.append(",");
							newColumns.append("`");
							newColumns.append(columnName);
							newColumns.append("`");
							orignalColumns.append(",");
							orignalColumns.append("`");
							orignalColumns.append(columnName);
							orignalColumns.append("`");
							break;
						}
					}
				} else {// add
					addColumns.add(columnName);
					XLog.v(TAG, "add " + columnName);
				}
			}
		}

		if (!deleteColumns.isEmpty() || !renameColumns.isEmpty()) {
			String tempTableName = "temp_" + tableName;
			db.execSQL(getCreateTableSQL(clazz, tempTableName));

			sql = "INSERT INTO " + tempTableName + "(" + newColumns.toString() + ") SELECT " + orignalColumns.toString() + " FROM "
					+ tableName;
			XLog.v(TAG, sql);
			db.execSQL(sql);
			sql = "DROP TABLE " + tableName;
			XLog.v(TAG, sql);
			db.execSQL(sql);
			createTable(clazz);
			sql = "INSERT INTO " + tableName + "(" + newColumns.toString() + ") SELECT " + newColumns.toString() + " FROM " + tempTableName;
			XLog.v(TAG, sql);
			db.execSQL(sql);
			sql = "DROP TABLE " + tempTableName;
			XLog.v(TAG, sql);
			db.execSQL(sql);
		} else if (!addColumns.isEmpty()) {
			for (String column : addColumns) {
				sql = "ALTER TABLE " + tableName + " ADD COLUMN " + column + " TEXT DEFAULT '';";
				db.execSQL(sql);
				XLog.v(TAG, sql);
			}
		}

		c.close();
	}

	private String[] dealEmptyValue(String[] args) {
		List<String> list = new ArrayList<String>();
		for (String string : args) {
			if (!TextUtils.isEmpty(string)) {
				list.add(string);
			}
		}
		String[] array = new String[list.size()];
		list.toArray(array);
		return array;
	}

}
