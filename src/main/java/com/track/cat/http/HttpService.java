package com.track.cat.http;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.fastjson.JSONObject;
import com.track.cat.core.Definiens;
import com.track.cat.handler.HandlerManager;
import com.track.cat.handler.Invocation;
import com.track.cat.handler.Result;
import com.track.cat.handler.interfaces.Invoker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class HttpService extends AbstractVerticle {
	private static Vertx gVertx = null;

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
		
		router.post("/ttt").handler(new Handler<RoutingContext>() {
			
			@Override
			public void handle(RoutingContext event) {
				System.out.println(event.getBodyAsString());
				event.response().end("ddd");
			}
		});
		
		ConcurrentMap<String, Invoker> workers = HandlerManager.getWorkers();
		workers.forEach((mapping, invoker) -> {
			router.post(mapping).handler(context -> {
				try {
					JSONObject jsonReq = new JSONObject();
					MultiMap httpParam = context.request().params();
					List<Map.Entry<String, String>> list = httpParam.entries();
					for (Map.Entry<String, String> e : list) {
						String key = e.getKey();
						String value = e.getValue();
						if (value == null || value.length() == 0) {
							continue;
						}
						jsonReq.put(key, value);
					}

					Invocation invocation = new Invocation();
					invocation.setAttachment(Invocation.MAPPING, mapping);
					invocation.setAttachment(Invocation.JSON_REQEST, jsonReq);
					Result result = HandlerManager.handler(invocation);
					context.response().end(result.getAttachment(Result.JSON_REPONSE).toString());
				} catch (Exception e) {
					context.response().end(ResultBuilder.build(0x1001).toString());
				}
			});
			
		});
		HttpServerOptions options = new HttpServerOptions();
		options.setReuseAddress(true);
		HttpServer server = gVertx.createHttpServer(options);
		server.requestHandler(router::accept).listen(Integer.valueOf(Definiens.PORT));

	}

}
