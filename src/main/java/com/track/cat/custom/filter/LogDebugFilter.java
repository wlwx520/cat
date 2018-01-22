package com.track.cat.custom.filter;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.annotation.Filter;
import com.track.cat.core.handler.interfaces.IFilter;
import com.track.cat.core.handler.interfaces.IInvoker;

import io.vertx.ext.web.FileUpload;

@Filter(index=0)
public class LogDebugFilter implements IFilter {
	private static final Logger LOGGER = Logger.getLogger(LogDebugFilter.class);

	@Override
	public Result invoke(IInvoker invoker, Invocation invocation) {
		String mapping = invocation.getAttachment(Invocation.MAPPING);
		Map<String, String> request = invocation.getAttachment(Invocation.REQUEST);
		Set<FileUpload> fileUploads = invocation.getAttachment(Invocation.UPLOAD_FILES);
		StringBuilder debug = new StringBuilder();
		debug.append("\n");
		debug.append("mapping = ");
		debug.append(mapping);
		debug.append("\n");
		debug.append("request = ");
		debug.append(request);
		debug.append("\n");
		if (fileUploads != null && !fileUploads.isEmpty()) {
			debug.append("fileUploads size = ");
			debug.append(fileUploads.size());
			fileUploads.forEach(fileUpload -> {
				debug.append("\n");
				debug.append("\t");
				debug.append("contentType = ");
				debug.append(fileUpload.contentType());
				debug.append(",");
				debug.append("name = ");
				debug.append(fileUpload.name());
				debug.append(",");
				debug.append("size = ");
				debug.append(fileUpload.size());
				debug.append(",");
				debug.append("file = ");
				debug.append(fileUpload.fileName());
				debug.append(",");
				debug.append("uploadedFile = ");
				debug.append(fileUpload.uploadedFileName());
			});
		} else {
			debug.append("fileUploads size = 0");
		}
		LOGGER.debug(debug.toString());
		return invoker.invoke(invocation);
	}

}
