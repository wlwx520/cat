package com.track.cat.app;

import com.track.cat.core.FilterManager;
import com.track.cat.core.HandlerManager;
import com.track.cat.core.LoggerManager;
import com.track.cat.core.http.HttpService;
import com.track.cat.persistent.PersistentManager;

public class Test {
	public static void main(String[] args) {
		LoggerManager.init("config/log4j.properties");

		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
		PersistentManager.init();

	}
}
