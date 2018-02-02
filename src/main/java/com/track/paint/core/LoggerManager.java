package com.track.paint.core;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

import com.track.paint.core.exception.SystemException;

public class LoggerManager {
	public static void init(String path) {
		try (FileInputStream istream = new FileInputStream(path)) {
			Properties props = new Properties();
			props.load(istream);
			PropertyConfigurator.configure(props);
		} catch (Exception e) {
			throw new SystemException(e);
		}
	}
}
