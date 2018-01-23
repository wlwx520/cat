package com.track.cat.core;

import java.io.File;

import org.dom4j.Element;

import com.track.cat.util.FileUtil;
import com.track.cat.util.XmlUtil;

public class Definiens {
	public static final Element ROOT_ELEMENT = XmlUtil
			.get(FileUtil.getAppRoot() + File.separator + "config" + File.separator + "context.xml");

	public static final String PORT = get("port");
	public static final String SERVICE_PACKAGE = get("service-package");
	public static final String FILTER_PACKAGE = get("filter-package");
	public static final String HTTP_CHANNEL_SIZE = get("http-channel-size");

	public static String get(String key) {
		return ROOT_ELEMENT.elementText(key);
	}
}
