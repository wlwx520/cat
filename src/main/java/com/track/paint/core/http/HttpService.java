package com.track.paint.core.http;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.track.paint.core.BaseHandler;
import com.track.paint.core.Definiens;
import com.track.paint.core.HandlerManager;
import com.track.paint.core.HttpMethod;
import com.track.paint.core.Invocation;
import com.track.paint.core.Result;
import com.track.paint.util.FileUtil;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class HttpService extends AbstractVerticle {
	private static final Logger LOGGER = Logger.getLogger(HttpService.class);
	private static Vertx gVertx = null;
	private static String webPath = Definiens.WEB_PATH;
	private static String uploadPath = Definiens.UPLOAD_PATH;

	public static void init() {
		VertxOptions vp = new VertxOptions();
		vp.setEventLoopPoolSize(64);
		vp.setMaxEventLoopExecuteTime(20000000);
		vp.setBlockedThreadCheckInterval(20000000);
		gVertx = Vertx.vertx(vp);
		DeploymentOptions p = new DeploymentOptions();
		p.setInstances(Integer.valueOf(Definiens.HTTP_CHANNEL_SIZE));
		gVertx.deployVerticle(HttpService.class.getName(), p);

		new File(webPath).mkdirs();
		new File(uploadPath).mkdirs();

		LOGGER.info("http channel size = " + Definiens.HTTP_CHANNEL_SIZE);
		LOGGER.info("system upload root = " + uploadPath);
		LOGGER.info("system web root = " + FileUtil.getAppRoot() + File.separator + webPath);
	}

	@Override
	public void start() {
		Router router = Router.router(gVertx);

		router.route().handler(BodyHandler.create().setUploadsDirectory(uploadPath));

		ConcurrentMap<String, BaseHandler> workers = HandlerManager.getWorkers();
		workers.forEach((mapping, handler) -> {
			HttpMethod[] methods = handler.getMethods();
			if (methods != null && methods.length > 0) {
				List<HttpMethod> asList = Arrays.asList(methods);
				if (asList.contains(HttpMethod.GET)) {
					get(router, mapping);
				}
				if (asList.contains(HttpMethod.POST)) {
					post(router, mapping);
				}
				if (asList.contains(HttpMethod.FILE)) {
					file(router, mapping);
				}
				if (asList.contains(HttpMethod.DOWNLOAD)) {
					downLoad(router, mapping);
				}
			}
		});

		router.route().handler(StaticHandler.create().setWebRoot(webPath).setCachingEnabled(false));

		HttpServerOptions options = new HttpServerOptions();
		options.setReuseAddress(true);
		HttpServer server = gVertx.createHttpServer(options);
		server.requestHandler(router::accept).listen(Integer.valueOf(Definiens.PORT));
	}

	private void post(Router router, String mapping) {
		router.post(mapping).handler(context -> {
			HttpServerResponse response = context.response();
			Map<String, String> param = new HashMap<>();

			String type = context.request().getHeader("Content-Type");
			if (type != null && (type.contains("text/plain") || type.contains("application/json;charset=UTF-8"))) {
				try {
					JSONObject jsonReq = JSONObject.parseObject(context.getBodyAsString());
					jsonReq.forEach((k, v) -> {
						param.put(k, v.toString());
					});
				} catch (Exception e) {
					response.end(ResultBuilder.buildResult(0x10001).getAttachment(Result.RESPONSE).toString());
					return;
				}
			} else {
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
			}

			Invocation invocation = new Invocation();
			invocation.setAttachment(Invocation.MAPPING, mapping);
			invocation.setAttachment(Invocation.REQUEST, param);
			Result result = HandlerManager.handler(invocation);
			// HttpServerRequest request = context.request();
			// response.putHeader("Access-Control-Allow-Origin",
			// request.getHeader("Origin"));
			// response.putHeader("Access-Control-Allow-Credentials", "true");
			// response.putHeader("P3P", "CP=CAO PSA OUR");
			// response.putHeader("Content-Type",
			// "application/x-www-form-urlencoded; charset=utf-8");
			// if (request.getHeader("Access-Control-Request-Method") != null &&
			// "OPTIONS".equals(request.method())) {
			// response.putHeader("Access-Control-Allow-Methods",
			// "POST,GET,TRACE,OPTIONS");
			// response.putHeader("Access-Control-Allow-Headers",
			// "Content-Type,Origin,Accept");
			// response.putHeader("Access-Control-Max-Age", "120");
			// }
			response.end(result.getAttachment(Result.RESPONSE).toString());
		});

	}

	private void get(Router router, String mapping) {
		router.get(mapping).handler(context -> {
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
			HttpServerResponse response = context.response();
			// HttpServerRequest request = context.request();
			// response.putHeader("Access-Control-Allow-Origin",
			// request.getHeader("Origin"));
			// response.putHeader("Access-Control-Allow-Credentials", "true");
			// response.putHeader("P3P", "CP=CAO PSA OUR");
			// response.putHeader("Content-Type",
			// "application/x-www-form-urlencoded; charset=utf-8");
			// if (request.getHeader("Access-Control-Request-Method") != null &&
			// "OPTIONS".equals(request.method())) {
			// response.putHeader("Access-Control-Allow-Methods",
			// "POST,GET,TRACE,OPTIONS");
			// response.putHeader("Access-Control-Allow-Headers",
			// "Content-Type,Origin,Accept");
			// response.putHeader("Access-Control-Max-Age", "120");
			// }
			response.end(result.getAttachment(Result.RESPONSE).toString());
		});
	}

	private void file(Router router, String mapping) {
		router.post(mapping).handler(context -> {
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

			Set<FileUpload> fileUploads = context.fileUploads();

			Invocation invocation = new Invocation();
			invocation.setAttachment(Invocation.MAPPING, mapping);
			invocation.setAttachment(Invocation.REQUEST, param);
			invocation.setAttachment(Invocation.UPLOAD_FILES, fileUploads);
			Result result = HandlerManager.handler(invocation);

			HttpServerResponse response = context.response();
			// HttpServerRequest request = context.request();
			// response.putHeader("Access-Control-Allow-Origin",
			// request.getHeader("Origin"));
			// response.putHeader("Access-Control-Allow-Credentials", "true");
			// response.putHeader("P3P", "CP=CAO PSA OUR");
			// response.putHeader("Content-Type",
			// "application/x-www-form-urlencoded; charset=utf-8");
			// if (request.getHeader("Access-Control-Request-Method") != null &&
			// "OPTIONS".equals(request.method())) {
			// response.putHeader("Access-Control-Allow-Methods",
			// "POST,GET,TRACE,OPTIONS");
			// response.putHeader("Access-Control-Allow-Headers",
			// "Content-Type,Origin,Accept");
			// response.putHeader("Access-Control-Max-Age", "120");
			// }
			response.end(result.getAttachment(Result.RESPONSE).toString());
		});
	}

	private void downLoad(Router router, String mapping) {
		router.get(mapping).handler(context -> {
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

			HttpServerResponse response = context.response();
			// HttpServerRequest request = context.request();
			// response.putHeader("Access-Control-Allow-Origin",
			// request.getHeader("Origin"));
			// response.putHeader("Access-Control-Allow-Credentials", "true");
			// response.putHeader("P3P", "CP=CAO PSA OUR");
			// response.putHeader("Content-Type",
			// "application/x-www-form-urlencoded; charset=utf-8");
			// if (request.getHeader("Access-Control-Request-Method") != null &&
			// "OPTIONS".equals(request.method())) {
			// response.putHeader("Access-Control-Allow-Methods",
			// "POST,GET,TRACE,OPTIONS");
			// response.putHeader("Access-Control-Allow-Headers",
			// "Content-Type,Origin,Accept");
			// response.putHeader("Access-Control-Max-Age", "120");
			// }
			byte[] attachment = (byte[]) result.getAttachment(Result.DOWNLOAD);
			response.end(Buffer.buffer(attachment));
		});
	}

}
