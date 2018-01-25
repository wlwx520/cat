package com.track.cat.app;

import org.apache.log4j.Logger;

import com.track.cat.core.FilterManager;
import com.track.cat.core.HandlerManager;
import com.track.cat.core.LoggerManager;
import com.track.cat.core.http.HttpService;
import com.track.cat.persistent.PersistentManager;

public class App {
	private static final Logger LOGGER = Logger.getLogger(App.class);

	public static void main(String[] args) {
		LoggerManager.init("config/log4j.properties");

		LOGGER.info("the system is initializing");
		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
		PersistentManager.init();
	}
}
