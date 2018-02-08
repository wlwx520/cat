package com.track.paint.persistent.helper;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbcp.BasicDataSource;

import com.track.paint.core.Definiens;
import com.track.paint.core.exception.SystemException;
import com.track.paint.util.FileUtil;

public class DBHelper {
	// INTEGER TEXT REAL PRIMARY KEY AUTOINCREMENT
	public static Connection getConnection() {
		try {
			BasicDataSource bds = new BasicDataSource();
			bds.setDriverClassName("org.sqlite.JDBC");
			bds.setInitialSize(1);
			bds.setMinIdle(1);
			bds.setMaxIdle(1);
			bds.setMaxActive(1);
			String db = FileUtil.getAppRoot() + File.separator + Definiens.DB_PATH;
			FileUtil.createDirAndFileIfNotExists(new File(db));
			bds.setUrl("jdbc:sqlite:" + db);
			return bds.getConnection();
		} catch (SQLException e) {
			throw new SystemException(e);
		}
	}
}
