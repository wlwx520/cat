package com.track.cat.http;

import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.track.cat.handler.HandlerManager;
import com.track.cat.handler.interfaces.Invoker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class HttpService extends AbstractVerticle {
	private static Logger LOGGER = Logger.getLogger(HttpService.class);

	private static Vertx gVertx = null;
	private HandlerManager handlerManager = HandlerManager.instance();

	public static void main(String[] args) {
		HttpService.init();
	}

	public static void init() {
		System.setProperty("vertx.disableFileCaching", "true");
		System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4jLogDelegateFactory");
		VertxOptions vp = new VertxOptions();
		vp.setEventLoopPoolSize(64);
		vp.setMaxEventLoopExecuteTime(20000000);
		vp.setBlockedThreadCheckInterval(20000000);
		gVertx = Vertx.vertx(vp);
		DeploymentOptions p = new DeploymentOptions();
		p.setInstances(64);
		gVertx.deployVerticle(HttpService.class.getName(), p);
	}

	@Override
	public void start() {
		Router router = Router.router(gVertx);

		// TODO
		ConcurrentMap<String, Invoker> workers = handlerManager.getWorkers();
		workers.forEach((mapping, invoker) -> {
			router.post(mapping).handler(context -> {
				String body = context.getBodyAsString("utf-8");
				try {
					JSONObject invocation = JSONObject.parseObject(body);
					// TODO
				} catch (Exception e) {
					context.response().end("cuo l ");
				}
			});
		});

		HttpServerOptions options = new HttpServerOptions();
		options.setReuseAddress(true);
		HttpServer server = gVertx.createHttpServer(options);
		server.requestHandler(router::accept).listen(8080);

	}

}
