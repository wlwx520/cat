package com.track.cat.app;

import com.track.cat.handler.FilterManager;
import com.track.cat.handler.HandlerManager;
import com.track.cat.http.HttpService;

public class App {
	public static void main(String[] args) {
		FilterManager.init();
		HandlerManager.init();
		HttpService.init();
	}
}
