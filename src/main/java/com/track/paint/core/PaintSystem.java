package com.track.paint.core;

import java.io.File;

import org.apache.log4j.Logger;

import com.track.paint.core.http.HttpService;
import com.track.paint.persistent.PersistentManager;
import com.track.paint.util.FileUtil;

public class PaintSystem {
	private static final Logger LOGGER = Logger.getLogger(PaintSystem.class);

	public static void start(String path) {
		Definiens.init(path);
		LoggerManager.init(Definiens.LOG4J_PATH);
		LOGGER.info("the system is initializing");

		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
		PersistentManager.init();

		new Thread(() -> {
			while (true) {
				FileUtil.deleteFile(new File(Definiens.UPLOAD_PATH), false);
				try {
					Thread.sleep(5 * 60_000);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
}
