package com.track.cat.core;

import java.io.File;

import org.dom4j.Element;

import com.track.cat.util.FileUtil;
import com.track.cat.util.XmlUtil;

public class Definiens {
	public static final String PORT = get("port");
	public static final String SERVICE_PACKAGE = get("scan-package");

	public static String get(String key) {
		Element rootEle = XmlUtil
				.get(FileUtil.getAppRoot() + File.separator + "config" + File.separator + "applicationContext.xml");
		return rootEle.elementText(key);
	}
}