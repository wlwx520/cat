package com.track.paint.persistent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.track.paint.core.Definiens;
import com.track.paint.core.exception.SystemException;
import com.track.paint.persistent.annotation.Column;
import com.track.paint.persistent.annotation.Persistent;
import com.track.paint.persistent.annotation.PrimaryKeyAutoincrement;
import com.track.paint.persistent.annotation.SimpleRelation;
import com.track.paint.persistent.helper.DBHelper;
import com.track.paint.util.CheckUtil;
import com.track.paint.util.FileUtil;
import com.track.paint.util.Reference;

public class PersistentManager {
	private static final Logger LOGGER = Logger.getLogger(PersistentManager.class);
	private static final Map<String, Class<?>> PERSISTENT_TEMPLATES = new ConcurrentHashMap<>();
	private static final Object LOCK = new Object();
	public static final String NULL = "Persisten Null";

	private static final PersistentManager instance = new PersistentManager();

	public static PersistentManager instance() {
		return instance;
	}

	public static void init() {
		if (Definiens.DB_CLEAR.equals("true")) {
			File file = new File(FileUtil.getAppRoot() + File.separator + Definiens.DB_PATH);
			if (file.exists()) {
				file.delete();
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String path = FileUtil.getAppRoot() + File.separator + "src" + File.separator + "main" + File.separator
					+ "java" + File.separator + Definiens.PERSISTENT_BEAN_PACKAGE.replaceAll("\\.", "/");

			LOGGER.info("scan the package to find persistented bean in " + path);
			instance()._scan(new File(path), "");
		}
	}

	private void _scan(File root, String parent) {
		FileUtil.subFile(root).forEach(file -> {
			String name = file.getName();
			instance().addBeanTable(parent + "." + name);
		});

		FileUtil.subDir(root).forEach(file -> {
			String name = file.getName();
			_scan(file, parent + "." + name);
		});
	}

	private boolean addBeanTable(String name) {
		try {
			Class<?> clz = Class.forName(Definiens.PERSISTENT_BEAN_PACKAGE + name.replace(".java", ""));
			addBeanTable(clz);
			return true;
		} catch (ClassNotFoundException e) {
			throw new SystemException(e);
		}

	}

	private boolean addBeanTable(Class<?> clz) {
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
				throw new SystemException("table name already exists");
			}

			PERSISTENT_TEMPLATES.put(table, clz);

			StringBuilder sql = new StringBuilder();

			sql.append("CREATE TABLE " + table + "(");
			sql.append(PersistentBean.ID + " INTEGER PRIMARY KEY AUTOINCREMENT");

			Field[] fields = clz.getDeclaredFields();
			if (fields == null || fields.length == 0) {

			} else {
				for (Field field : fields) {
					field.setAccessible(true);
					Column column = field.getAnnotation(Column.class);
					SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
					PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);

					if (autoincrement != null) {
						CheckUtil.checkPrimaryKey(field);
					} else if (column != null) {
						buildColumn(field, sql);
					} else if (simpleRelation != null) {
						buildSimpleRelation(field, sql);
					}
				}
			}
			sql.append(")");

			Connection connection = DBHelper.getConnection();
			PreparedStatement pst = connection.prepareStatement(sql.toString());
			pst.execute();

			return true;
		} catch (SQLException e) {
			throw new SystemException(e);
		}

	}

	private void buildSimpleRelation(Field field, StringBuilder sql) {
		if (!List.class.isAssignableFrom(field.getType())) {
			throw new SystemException("field must be assignable from List");
		}

		Type genericType = field.getGenericType();
		if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
			throw new SystemException("field must be assignable from List");
		}
		Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
		if (types == null || types.length != 1) {
			throw new SystemException("field must be assignable from List");
		}

		Class<?> listSubClz = getClass(types[0]);
		if (!(CheckUtil.checkNumber(listSubClz) || CheckUtil.checkText(listSubClz)
				|| CheckUtil.checkDecimal(listSubClz))) {
			boolean flg = addBeanTable(listSubClz);
			if (!flg) {
				throw new SystemException("class of " + listSubClz.getName() + " can not be persistented");
			}
		}
		sql.append("," + field.getName() + " TEXT");
	}

	private void buildColumn(Field field, StringBuilder sql) {
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
				throw new SystemException("class of " + type.getName() + " can not be persistented");
			}
		}
	}

	public <T extends PersistentBean> long save(T bean) {
		if (bean.cat_static_table_primary_key == -1) {
			return create(bean);
		} else {
			return update(bean);
		}
	}

	private <T extends PersistentBean> long create(T bean) {
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();
		StringBuilder sqlType = new StringBuilder();
		StringBuilder sqlValue = new StringBuilder();

		sql.append("INSERT INTO " + table + "(");

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			Reference<Boolean> ref = new Reference<Boolean>(false);
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);

				if (column != null) {
					insertColumn(field, bean, sqlType, sqlValue, ref);
				} else if (simpleRelation != null) {
					insertSimpleRelation(field, bean, sqlType, sqlValue, ref);
				}
			}
		}

		sql.append(sqlType);
		sql.append(") values (");
		sql.append(sqlValue);

		sql.append(")");

		synchronized (LOCK) {
			try (Connection connection = DBHelper.getConnection();
					PreparedStatement pst = connection.prepareStatement(sql.toString());) {

				pst.execute();

				PreparedStatement pst2 = connection.prepareStatement("select last_insert_rowid()");
				ResultSet resultSet = pst2.executeQuery();
				if (resultSet.next()) {
					long id = resultSet.getLong(1);
					bean.cat_static_table_primary_key = id;
					return id;
				} else {
					throw new SystemException("system error");
				}
			} catch (SQLException e) {
				throw new SystemException(e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void insertSimpleRelation(Field field, PersistentBean bean, StringBuilder sqlType, StringBuilder sqlValue,
			Reference<Boolean> ref) {
		Class<?> type = field.getType();
		if (ref.get()) {
			sqlType.append(",");
			sqlValue.append(",");
		}

		assert (List.class.isAssignableFrom(type));

		try {
			List<PersistentBean> list = (List<PersistentBean>) field.get(bean);

			sqlValue.append("'");
			if (list != null && !list.isEmpty()) {
				StringBuilder ids = new StringBuilder();
				boolean flg = false;
				for (PersistentBean item : list) {
					if (flg) {
						ids.append("#");
					}
					long id = save(item);
					ids.append(id);
					flg = true;
				}

				sqlType.append(field.getName());
				sqlValue.append(ids.toString());
			}
			sqlValue.append("'");
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SystemException(e);
		}

		ref.set(true);
	}

	private void insertColumn(Field field, PersistentBean bean, StringBuilder sqlType, StringBuilder sqlValue,
			Reference<Boolean> ref) {
		Class<?> type = field.getType();
		if (ref.get()) {
			sqlType.append(",");
			sqlValue.append(",");
		}

		sqlType.append(field.getName());

		try {
			if (CheckUtil.checkText(type)) {
				sqlValue.append("'");
				sqlValue.append(field.get(bean) == null ? NULL : field.get(bean).toString());
				sqlValue.append("'");
			} else if (CheckUtil.checkNumber(type) || CheckUtil.checkDecimal(type)) {
				sqlValue.append(field.get(bean).toString());
			} else {
				if (!PersistentBean.class.isAssignableFrom(type)) {
					throw new SystemException("field of " + field + " can not be persistented");
				}

				long id = save((PersistentBean) field.get(bean));
				sqlValue.append(id);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SystemException(e);
		}

		ref.set(true);
	}

	private <T extends PersistentBean> long update(T bean) {
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("UPDATE " + table + " SET ");

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			Reference<Boolean> ref = new Reference<Boolean>(false);
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);

				if (column != null) {
					updateColumn(field, bean, sql, ref);
				} else if (simpleRelation != null) {
					updateSimpleRelation(field, bean, sql, ref);
				}
			}
		}

		sql.append(" WHERE " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return bean.cat_static_table_primary_key;

	}

	@SuppressWarnings("unchecked")
	private void updateSimpleRelation(Field field, PersistentBean bean, StringBuilder sql, Reference<Boolean> ref) {
		Class<?> type = field.getType();
		if (ref.get()) {
			sql.append(",");
		}

		assert (List.class.isAssignableFrom(type));

		try {
			List<PersistentBean> list = (List<PersistentBean>) field.get(bean);
			sql.append(field.getName());

			sql.append(" = ");
			sql.append("'");
			if (list != null && !list.isEmpty()) {
				StringBuilder ids = new StringBuilder();
				boolean flg = false;
				for (PersistentBean item : list) {
					if (flg) {
						ids.append("#");
					}
					long id = save(item);
					ids.append(id);
					flg = true;
				}

				sql.append(ids.toString());
			}
			sql.append("'");

		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SystemException(e);
		}

		ref.set(true);

	}

	private void updateColumn(Field field, PersistentBean bean, StringBuilder sql, Reference<Boolean> ref) {
		Class<?> type = field.getType();
		if (ref.get()) {
			sql.append(",");
		}

		sql.append(field.getName());

		sql.append(" = ");

		try {
			if (CheckUtil.checkText(type)) {
				sql.append("'");
				sql.append(field.get(bean) == null ? NULL : field.get(bean).toString());
				sql.append("'");
			} else if (CheckUtil.checkNumber(type) || CheckUtil.checkDecimal(type)) {
				sql.append(field.get(bean).toString());
			} else {
				if (!PersistentBean.class.isAssignableFrom(type)) {
					throw new SystemException("field of " + field + " can not be persistented");
				}

				long id = save((PersistentBean) field.get(bean));
				sql.append(id);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new SystemException(e);
		}

		ref.set(true);
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentBean> List<T> queryExt(T bean, String extCondition, List<String> conditions) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}

		List<T> result = new ArrayList<>();
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM " + table + " WHERE 1 = 1");

		if (extCondition != null && !extCondition.trim().equals("")) {
			sql.append(" and " + extCondition);
		}

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				T t = (T) bean.getClass().newInstance();
				t.cat_static_table_primary_key = rs.getLong(PersistentBean.ID);
				if (fields != null && fields.length > 0) {
					for (Field field : fields) {
						if (field.getAnnotation(Column.class) != null) {
							SetField(field, t, rs);
						} else if (field.getAnnotation(PrimaryKeyAutoincrement.class) != null) {
							if (field.getType().isAssignableFrom(int.class)
									|| field.getType().isAssignableFrom(Integer.class)) {
								field.set(t, Integer.valueOf(String.valueOf(t.cat_static_table_primary_key)));
							} else {
								field.set(t, t.cat_static_table_primary_key);
							}
						} else if (field.getAnnotation(SimpleRelation.class) != null) {
							setSimpleRelationField(rs, t, field);
						}
					}
				}
				result.add(t);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentBean> List<T> queryExtPage(T bean, String extCondition, List<String> conditions,
			int offset, int limit) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}

		List<T> result = new ArrayList<>();
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM " + table + " WHERE 1 = 1");

		if (extCondition != null && !extCondition.trim().equals("")) {
			sql.append(" and " + extCondition);
		}

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		sql.append(" LIMIT " + limit + " OFFSET " + offset);

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				T t = (T) bean.getClass().newInstance();
				t.cat_static_table_primary_key = rs.getLong(PersistentBean.ID);
				if (fields != null && fields.length > 0) {
					for (Field field : fields) {
						if (field.getAnnotation(Column.class) != null) {
							SetField(field, t, rs);
						} else if (field.getAnnotation(PrimaryKeyAutoincrement.class) != null) {
							if (field.getType().isAssignableFrom(int.class)
									|| field.getType().isAssignableFrom(Integer.class)) {
								field.set(t, Integer.valueOf(String.valueOf(t.cat_static_table_primary_key)));
							} else {
								field.set(t, t.cat_static_table_primary_key);
							}
						} else if (field.getAnnotation(SimpleRelation.class) != null) {
							setSimpleRelationField(rs, t, field);
						}
					}
				}
				result.add(t);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public <T extends PersistentBean> long queryCountExt(T bean, String extCondition, List<String> conditions) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		long count = -1;
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(*) FROM " + table + " WHERE 1 = 1");

		if (extCondition != null && !extCondition.trim().equals("")) {
			sql.append(" and " + extCondition);
		}

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				count = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public <T extends PersistentBean> long queryCountExtPage(T bean, String extCondition, List<String> conditions,
			int offset, int limit) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}
		long count = -1;
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(*) FROM " + table + " WHERE 1 = 1");

		if (extCondition != null && !extCondition.trim().equals("")) {
			sql.append(" and " + extCondition);
		}

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		sql.append(" LIMIT " + limit + " OFFSET " + offset);

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				count = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentBean> List<T> query(T bean, String... conditions) {
		List<String> conditionList;
		if (conditions != null && conditions.length != 0) {
			conditionList = Arrays.asList(conditions);
		} else {
			conditionList = new ArrayList<>();
		}

		List<T> result = new ArrayList<>();
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT * FROM " + table + " WHERE 1 = 1");

		if (conditionList.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditionList, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditionList, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditionList, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			while (rs.next()) {
				T t = (T) bean.getClass().newInstance();
				t.cat_static_table_primary_key = rs.getLong(PersistentBean.ID);
				if (fields != null && fields.length > 0) {
					for (Field field : fields) {
						if (field.getAnnotation(Column.class) != null) {
							SetField(field, t, rs);
						} else if (field.getAnnotation(PrimaryKeyAutoincrement.class) != null) {
							if (field.getType().isAssignableFrom(int.class)
									|| field.getType().isAssignableFrom(Integer.class)) {
								field.set(t, Integer.valueOf(String.valueOf(t.cat_static_table_primary_key)));
							} else {
								field.set(t, t.cat_static_table_primary_key);
							}
						} else if (field.getAnnotation(SimpleRelation.class) != null) {
							setSimpleRelationField(rs, t, field);
						}
					}
				}
				result.add(t);
			}
		} catch (SQLException | InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return result;
	}

	public <T extends PersistentBean> long queryCount(T bean, List<String> conditions) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}

		long count = -1;
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(*) FROM " + table + " WHERE 1 = 1");

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				count = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public <T extends PersistentBean> long queryCountPage(T bean, List<String> conditions, int offset, int limit) {
		if (conditions == null) {
			conditions = new ArrayList<>();
		}

		long count = -1;
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT COUNT(*) FROM " + table + " WHERE 1 = 1");

		if (conditions.contains(PersistentBean.ID)) {
			sql.append(" and " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);
		}

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				Column column = field.getAnnotation(Column.class);
				PrimaryKeyAutoincrement autoincrement = field.getAnnotation(PrimaryKeyAutoincrement.class);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (column != null) {
						queryColumn(bean, conditions, sql, field);
					} else if (autoincrement != null) {
						queryKey(bean, conditions, sql, field);
					} else if (simpleRelation != null) {
						querySimpleRelation(bean, conditions, sql, field);
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		sql.append(" LIMIT " + limit + " OFFSET " + offset);

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			ResultSet rs = pst.executeQuery();

			if (rs.next()) {
				count = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	@SuppressWarnings("unchecked")
	private <T extends PersistentBean> void querySimpleRelation(T bean, List<String> conditionList, StringBuilder sql,
			Field field) throws IllegalAccessException {
		assert (List.class.isAssignableFrom(field.getType()));
		if (conditionList.contains(field.getName())) {

			List<? extends PersistentBean> list = (List<? extends PersistentBean>) field.get(bean);
			StringBuilder ids = new StringBuilder();
			ids.append("'");
			if (list != null && !list.isEmpty()) {
				boolean flg = false;
				for (PersistentBean item : list) {
					if (flg) {
						ids.append(",");
					}
					ids.append(item.cat_static_table_primary_key);
					flg = true;
				}
			}
			ids.append("'");

			sql.append(" and " + field.getName() + " = " + ids.toString());
		}
	}

	private <T extends PersistentBean> void queryColumn(T bean, List<String> conditionList, StringBuilder sql,
			Field field) throws IllegalAccessException {
		if (conditionList.contains(field.getName())) {
			Object listF = field.get(bean);
			Object list = listF;
			if (CheckUtil.checkText(field.getType())) {
				sql.append(" and " + field.getName() + " = ");
				sql.append("'");
				sql.append(list == null ? NULL : list.toString());
				sql.append("'");
			} else {
				sql.append(" and " + field.getName() + " = " + list.toString());
			}
		}
	}

	private <T extends PersistentBean> void queryKey(T bean, List<String> conditionList, StringBuilder sql, Field field)
			throws IllegalAccessException {
		if (conditionList.contains(field.getName())) {
			Object listF = field.get(bean);
			Object list = listF;
			if (CheckUtil.checkText(field.getType())) {
				sql.append(" and " + PersistentBean.ID + " = ");
				sql.append("'");
				sql.append(list.toString());
				sql.append("'");
			} else {
				sql.append(" and " + PersistentBean.ID + " = " + list.toString());
			}
		}
	}

	private <T extends PersistentBean> void setSimpleRelationField(ResultSet rs, T t, Field field)
			throws SQLException, InstantiationException, IllegalAccessException {
		String ids = rs.getString(field.getName());
		if (ids != null && !ids.trim().equals("") || !ids.equals(NULL)) {
			String[] split = ids.split("#");
			if (!List.class.isAssignableFrom(field.getType())) {
				throw new SystemException("field must be assignable from List");
			}

			Type genericType = field.getGenericType();
			if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
				throw new SystemException("field must be assignable from List");
			}
			Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
			if (types == null || types.length != 1) {
				throw new SystemException("field must be assignable from List");
			}

			@SuppressWarnings("unchecked")
			Class<? extends PersistentBean> listSubClz = (Class<? extends PersistentBean>) getClass(types[0]);

			List<PersistentBean> listValue = new ArrayList<>();

			for (String sp : split) {
				PersistentBean newInstance = listSubClz.newInstance();
				newInstance.cat_static_table_primary_key = Long.valueOf(sp);
				List<PersistentBean> query = query(newInstance);
				assert (query != null);
				listValue.addAll(query);
			}
			field.set(t, listValue);
		}
	}

	private <T extends PersistentBean> void SetField(Field field, T t, ResultSet rs) {
		Class<?> type = field.getType();
		try {
			if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
				field.set(t, rs.getInt(field.getName()));
			} else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
				field.set(t, rs.getShort(field.getName()));
			} else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
				field.set(t, rs.getLong(field.getName()));
			} else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
				field.set(t, rs.getDouble(field.getName()));
			} else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
				field.set(t, rs.getFloat(field.getName()));
			} else if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
				field.set(t, rs.getBoolean(field.getName()));
			} else if (type.isAssignableFrom(char.class) || type.isAssignableFrom(Character.class)) {
				field.set(t, rs.getString(field.getName()).charAt(0));
			} else if (type.isAssignableFrom(String.class)) {
				String value = rs.getString(field.getName());
				field.set(t, value.equals(NULL) ? null : value);
			}
		} catch (IllegalArgumentException | IllegalAccessException | SQLException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends PersistentBean> boolean delete(T bean) {
		Class<? extends PersistentBean> clz = bean.getClass();
		Persistent persistent = clz.getAnnotation(Persistent.class);
		assert (persistent != null);
		String table = persistent.table();

		Field[] fields = clz.getDeclaredFields();
		if (fields != null && fields.length > 0) {
			for (Field field : fields) {
				field.setAccessible(true);
				SimpleRelation simpleRelation = field.getAnnotation(SimpleRelation.class);
				try {
					if (simpleRelation != null) {
						List<? extends PersistentBean> list = (List<? extends PersistentBean>) field.get(bean);
						if (list != null && !list.isEmpty()) {
							Type genericType = field.getGenericType();
							if (!ParameterizedType.class.isAssignableFrom(genericType.getClass())) {
								throw new SystemException("field must be assignable from List");
							}
							Type[] types = ((ParameterizedType) genericType).getActualTypeArguments();
							if (types == null || types.length != 1) {
								throw new SystemException("field must be assignable from List");
							}

							Class<? extends PersistentBean> listSubClz = (Class<? extends PersistentBean>) getClass(
									types[0]);

							list.forEach(item -> {
								try {
									PersistentBean newInstance = listSubClz.newInstance();
									newInstance.cat_static_table_primary_key = item.cat_static_table_primary_key;

									delete(newInstance);
								} catch (Exception e) {
									e.printStackTrace();
								}
							});

						}
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					throw new SystemException(e);
				}
			}
		}

		StringBuilder sql = new StringBuilder();

		sql.append("DELETE FROM " + table + " WHERE " + PersistentBean.ID + " = " + bean.cat_static_table_primary_key);

		try (Connection connection = DBHelper.getConnection();
				PreparedStatement pst = connection.prepareStatement(sql.toString());) {

			return pst.execute();
		} catch (SQLException e) {
			throw new SystemException(e);
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
			throw new SystemException(e);
		}
	}

}
