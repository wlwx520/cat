package com.track.cat.custom.serivce;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.HttpMethod;
import com.track.cat.core.handler.annotation.Handler;
import com.track.cat.core.handler.annotation.Service;
import com.track.cat.core.handler.interfaces.IService;
import com.track.cat.core.http.ResultBuilder;

import io.vertx.ext.web.FileUpload;

@Service("/test")
public class TestService implements IService {
	private static final Logger LOGGER = Logger.getLogger(TestService.class);

	@Handler(value = "/text", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		Map<String, String> data = invocation.getAttachment(Invocation.REQUEST);
		LOGGER.debug("/test/text request -----> " + data.toString());
		return ResultBuilder.buildResult(0);
	}

	@Handler(value = "/file", method = { HttpMethod.FILE })
	public Result test2(Invocation invocation) {
		Map<String, String> data = invocation.getAttachment(Invocation.REQUEST);
		LOGGER.debug("/test/file request -----> " + data.toString());
		Set<FileUpload> fileUploads = invocation.getAttachment(Invocation.UPLOAD_FILES);
		LOGGER.debug("/test/file fileSzie -----> " + fileUploads.size());
		return ResultBuilder.buildResult(0);
	}
}
