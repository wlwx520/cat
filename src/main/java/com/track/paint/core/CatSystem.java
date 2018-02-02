package com.track.paint.core;

import org.apache.log4j.Logger;

import com.track.paint.core.http.HttpService;
import com.track.paint.persistent.PersistentManager;

public class CatSystem {
	private static final Logger LOGGER = Logger.getLogger(CatSystem.class);

	public static void start(String path) {
		Definiens.init(path);
		LoggerManager.init(Definiens.LOG4J_PATH);
		LOGGER.info("the system is initializing");

		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
		PersistentManager.init();
	}
}
