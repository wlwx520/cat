package com.track.cat.persistent;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.track.cat.core.Definiens;
import com.track.cat.core.exception.CatSqlExcption;
import com.track.cat.persistent.annotation.Column;
import com.track.cat.persistent.annotation.ComplexRelation;
import com.track.cat.persistent.annotation.Persistent;
import com.track.cat.persistent.annotation.PrimaryKeyAutoincrement;
import com.track.cat.persistent.annotation.SimpleRelation;
import com.track.cat.persistent.helper.DBHelper;
import com.track.cat.util.CheckUtil;
import com.track.cat.util.FileUtil;

public class PersistentManager {
	private static final Logger LOGGER = Logger.getLogger(PersistentManager.class);
	private static final Map<String, Class<?>> PERSISTENT_TEMPLATES = new ConcurrentHashMap<>();

	public static void main(String[] args) {
		init();
	}

	public static void init() {
		String path = FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator + "java"
				+ File.separator + Definiens.PERSISTENT_BEAN_PACKAGE.replaceAll("\\.", "/");

		LOGGER.info("scan the package to find persistented bean in " + path);
		_scan(new File(path), "");
	}

	private static void _scan(File root, String parent) {
		FileUtil.subFile(root).forEach(file -> {
			String name = file.getName();
			addBeanTable(parent + "." + name);
		});

		FileUtil.subDir(root).forEach(file -> {
			String name = file.getName();
			_scan(file, parent + "." + name);
		});
	}

	private static boolean addBeanTable(String name) {
		try {
			Class<?> clz = Class.forName(Definiens.PERSISTENT_BEAN_PACKAGE + name.replace(".java", ""));
			addBeanTable(clz);
			return true;
		} catch (ClassNotFoundException e) {
			throw new CatSqlExcption(e);
		}

	}

	private static boolean addBeanTable(Class<?> clz) {
		try {
			if (!PersistentBean.class.isAssignableFrom(clz)) {
				return false;
			}

			Persistent persistent = clz.getAnnotation(Persistent.class);
			if (persistent == null) {
				return false;
			}

			if (PERSISTENT_TEMPLATES.containsValue(clz)) {
				return true;
			}

			String table = persistent.table();

			if (PERSISTENT_TEMPLATES.containsKey(table)) {
				throw new CatSqlExcption("table name already exists");
			}

			PERSISTENT_TEMPLATES.put(table, clz);

			StringBuilder sql = new StringBuilder();

			sql.append("CREATE TABLE " + table + "(");
			sql.append(PersistentBean.ID + " INTEGER PRIMARY KEY AUTOINCREMENT");

			Field[] fields = clz.getDeclaredFields();
			if (fields == null || fields.length == 0) {

			} else {
				for (Field field : fields) {
					Column column = field.getAnnotation(Column.class);
					SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
					ComplexRelation complexRelation = field.getAnnotation(ComplexRelation.class);
					PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);

					if (autoincrement != null) {
						CheckUtil.checkPrimaryKey(field);
					} else if (column != null) {
						buildColumn(field, sql);
					} else if (simpleRelation != null) {
						buildSimpleRelation(field, sql);
					} else if (complexRelation != null) {
						buildComplexRelation(field, sql);
					}
				}
			}
			sql.append(")");

			Connection connection = DBHelper.getConnection();
			PreparedStatement pst = connection.prepareStatement(sql.toString());
			pst.execute();

			return true;
		} catch (SQLException e) {
			throw new CatSqlExcption(e);
		}

	}

	private static void buildComplexRelation(Field field, StringBuilder sql) {
		if (!List.class.isAssignableFrom(field.getType())) {
			throw new CatSqlExcption("field must be assignable from List");
		}

		Type genericType = field.getGenericType();
		if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
			throw new CatSqlExcption("field must be assignable from List");
		}
		Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
		if (types == null || types.length != 1) {
			throw new CatSqlExcption("field must be assignable from List");
		}

		Class<?> listSubClz = getClass(types[0]);

		if (CheckUtil.checkNumber(listSubClz) || CheckUtil.checkText(listSubClz)
				|| CheckUtil.checkDecimal(listSubClz)) {
			throw new CatSqlExcption("complex relation must be between an Object and another Object");
		}

		String otherFieldName = field.getAnnotation(ComplexRelation.class).value();

		Field otherField;
		try {
			otherField = listSubClz.getDeclaredField(otherFieldName);
		} catch (NoSuchFieldException | SecurityException e) {
			throw new CatSqlExcption(e);
		}
		if (otherField == null) {
			throw new CatSqlExcption("field of " + otherFieldName + " is not found in " + listSubClz.getName());
		}

		boolean flg = addBeanTable(listSubClz);
		if (!flg) {
			throw new CatSqlExcption("class of " + listSubClz.getName() + " can not be persistented");
		}

		buildRelationTable(field, otherField);

	}

	private static Map<String, String> relations = new ConcurrentHashMap<>();

	private static void buildRelationTable(Field field, Field otherField) {
		String name = field.toString().replaceAll("\\.", "").replaceAll(" ", "") + "_" + otherField.toString().replaceAll("\\.", "").replaceAll(" ", "");
		String otherName = otherField.toString().replaceAll("\\.", "").replaceAll(" ", "") + "_" + field.toString().replaceAll("\\.", "").replaceAll(" ", "");

		if (relations.containsKey(name) || relations.containsKey(otherName)) {
			return;
		}

		StringBuilder sql = new StringBuilder();

		sql.append("CREATE TABLE " + name + "(");
		sql.append(PersistentBean.ID + " INTEGER PRIMARY KEY AUTOINCREMENT");

		sql.append("," + field.getName() + " INTEGER");
		sql.append("," + otherField.getName() + " INTEGER");

		sql.append(")");

		try {
			Connection connection = DBHelper.getConnection();
			PreparedStatement pst = connection.prepareStatement(sql.toString());
			pst.execute();
		} catch (SQLException e) {
			throw new CatSqlExcption(e);
		}

		relations.put(name, name);
		relations.put(otherName, name);

	}

	private static void buildSimpleRelation(Field field, StringBuilder sql) {
		if (!List.class.isAssignableFrom(field.getType())) {
			throw new CatSqlExcption("field must be assignable from List");
		}

		Type genericType = field.getGenericType();
		if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
			throw new CatSqlExcption("field must be assignable from List");
		}
		Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
		if (types == null || types.length != 1) {
			throw new CatSqlExcption("field must be assignable from List");
		}

		Class<?> listSubClz = getClass(types[0]);
		if (!(CheckUtil.checkNumber(listSubClz) || CheckUtil.checkText(listSubClz)
				|| CheckUtil.checkDecimal(listSubClz))) {
			boolean flg = addBeanTable(listSubClz);
			if (!flg) {
				throw new CatSqlExcption("class of " + listSubClz.getName() + " can not be persistented");
			}
		}
		sql.append("," + field.getName() + " TEXT");
	}

	private static void buildColumn(Field field, StringBuilder sql) {
		Class<?> type = field.getType();
		if (CheckUtil.checkNumber(type)) {
			sql.append("," + field.getName() + " INTEGER");
		} else if (CheckUtil.checkDecimal(type)) {
			sql.append("," + field.getName() + " REAL");
		} else if (CheckUtil.checkText(type)) {
			sql.append("," + field.getName() + " TEXT");
		} else {
			sql.append("," + field.getName() + " INTEGER");
			boolean flg = addBeanTable(type);
			if (!flg) {
				throw new CatSqlExcption("class of " + type.getName() + " can not be persistented");
			}
		}
	}

	private static final String TYPE_CLASS_NAME_PREFIX = "class ";
	private static final String TYPE_INTERFACE_NAME_PREFIX = "interface ";

	private static String getClassName(Type type) {
		if (type == null) {
			return "";
		}
		String className = type.toString();
		if (className.startsWith(TYPE_CLASS_NAME_PREFIX)) {
			className = className.substring(TYPE_CLASS_NAME_PREFIX.length());
		} else if (className.startsWith(TYPE_INTERFACE_NAME_PREFIX)) {
			className = className.substring(TYPE_INTERFACE_NAME_PREFIX.length());
		}
		return className;
	}

	private static Class<?> getClass(Type type) {
		String className = getClassName(type);
		if (className == null || className.isEmpty()) {
			return null;
		}
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new CatSqlExcption(e);
		}
	}

}
