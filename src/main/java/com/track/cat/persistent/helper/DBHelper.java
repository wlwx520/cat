package com.track.cat.persistent.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.track.cat.core.Definiens;
import com.track.cat.core.exception.CatSqlExcption;
import com.track.cat.util.FileUtil;

public class DBHelper {
	// INTEGER TEXT REAL PRIMARY KEY AUTOINCREMENT
	public static Connection getConnection() {
		try {
			BasicDataSource bds = new BasicDataSource();
			bds.setDriverClassName("org.sqlite.JDBC");
			bds.setInitialSize(10);
			bds.setMinIdle(5);
			bds.setMaxIdle(20);
			bds.setMaxActive(30);
			String db = FileUtil.getAppRoot() + File.separator + Definiens.DB_PATH;
			FileUtil.createDirAndFileIfNotExists(new File(db));
			bds.setUrl("jdbc:sqlite:" + db);
			return bds.getConnection();
		} catch (SQLException e) {
			throw new CatSqlExcption(e);
		}
	}
}
