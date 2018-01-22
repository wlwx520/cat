package com.track.cat.core.http;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.track.cat.core.Definiens;
import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.HandlerManager;
import com.track.cat.core.handler.interfaces.IInvoker;
import com.track.cat.util.FileUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class HttpService extends AbstractVerticle {
	private static Vertx gVertx = null;

	public static void init() {
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

		String uploadPath = FileUtil.getAppRoot() + File.separator + "upload";
		router.route().handler(BodyHandler.create().setUploadsDirectory(uploadPath));

		ConcurrentMap<String, IInvoker> workers = HandlerManager.getWorkers();
		workers.forEach((mapping, invoker) -> {
			router.post(mapping).handler(context -> {
				System.out.println(context.request().getHeader("Content-Type"));
				System.out.println(mapping);
				Map<String, String> param = new HashMap<>();
				MultiMap httpParam = context.request().params();
				List<Map.Entry<String, String>> list = httpParam.entries();
				for (Map.Entry<String, String> e : list) {
					String key = e.getKey();
					String value = e.getValue();
					if (value == null || value.length() == 0) {
						continue;
					}
					param.put(key, value);
				}

				Invocation invocation = new Invocation();
				invocation.setAttachment(Invocation.MAPPING, mapping);
				invocation.setAttachment(Invocation.REQUEST, param);
				Result result = HandlerManager.handler(invocation);
				context.response().end(result.getAttachment(Result.RESPONSE).toString());
			});

		});
		HttpServerOptions options = new HttpServerOptions();
		options.setReuseAddress(true);
		HttpServer server = gVertx.createHttpServer(options);
		server.requestHandler(router::accept).listen(Integer.valueOf(Definiens.PORT));

	}

}
