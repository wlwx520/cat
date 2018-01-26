package com.track.cat.app;

import org.apache.log4j.Logger;

import com.track.cat.core.Definiens;
import com.track.cat.core.FilterManager;
import com.track.cat.core.HandlerManager;
import com.track.cat.core.LoggerManager;
import com.track.cat.core.http.HttpService;
import com.track.cat.persistent.PersistentManager;

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
