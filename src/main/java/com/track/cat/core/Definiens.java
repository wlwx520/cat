package com.track.cat.core;

import java.io.File;

import org.dom4j.Element;

import com.track.cat.util.FileUtil;
import com.track.cat.util.XmlUtil;

public class Definiens {

	public static Element ROOT_ELEMENT;

	public static String PORT;
	public static String SERVICE_PACKAGE;
	public static String FILTER_PACKAGE;
	public static String PERSISTENT_BEAN_PACKAGE;
	public static String HTTP_CHANNEL_SIZE;
	public static String DB_CLEAR;

	public static String LOG4J_PATH;
	public static String DB_PATH;
	public static String WEB_PATH;
	public static String UPLOAD_PATH;

	public static String get(String key) {
		return ROOT_ELEMENT.elementText(key);
	}

	public static void init(String path) {
		ROOT_ELEMENT = XmlUtil.get(FileUtil.getAppRoot() + File.separator + path);
		PORT = get("port");
		SERVICE_PACKAGE = get("service-package");
		FILTER_PACKAGE = get("filter-package");
		PERSISTENT_BEAN_PACKAGE = get("persistent-bean-package");
		HTTP_CHANNEL_SIZE = get("http-channel-size");
		DB_CLEAR = get("db-clear");
		LOG4J_PATH = get("log4j-path");
		DB_PATH = get("data-path");
		WEB_PATH = get("web-path");
		UPLOAD_PATH = get("upload-path");
	}
}
