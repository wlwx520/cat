package com.track.cat.core;

import java.io.File;

import org.dom4j.Element;

import com.track.cat.util.FileUtil;
import com.track.cat.util.XmlUtil;

public class Definiens {
	public static final String PORT = get("port");
	public static final String SERVICE_PACKAGE = get("service-package");
	public static final String FILTER_PACKAGE = get("filter-package");

	public static String get(String key) {
		Element rootEle = XmlUtil
				.get(FileUtil.getAppRoot() + File.separator + "config" + File.separator + "context.xml");
		return rootEle.elementText(key);
	}
}
