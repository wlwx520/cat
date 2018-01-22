package com.track.cat.app;

import com.track.cat.core.handler.FilterManager;
import com.track.cat.core.handler.HandlerManager;
import com.track.cat.core.http.HttpService;

public class App {
	public static void main(String[] args) {
		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
	}
}
