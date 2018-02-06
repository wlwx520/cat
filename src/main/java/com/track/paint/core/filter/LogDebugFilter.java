package com.track.paint.core.filter;

import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.track.paint.core.Invocation;
import com.track.paint.core.Result;
import com.track.paint.core.annotation.Filter;
import com.track.paint.core.interfaces.IFilter;
import com.track.paint.core.interfaces.IInvoker;

import io.vertx.ext.web.FileUpload;

@Filter(index = 0)
public class LogDebugFilter implements IFilter {
	private static final Logger LOGGER = Logger.getLogger(LogDebugFilter.class);

	@Override
	public Result invoke(IInvoker invoker, Invocation invocation) {
		String mapping = invocation.getAttachment(Invocation.MAPPING);
		Map<String, String> request = invocation.getAttachment(Invocation.REQUEST);
		Set<FileUpload> fileUploads = invocation.getAttachment(Invocation.UPLOAD_FILES);
		StringBuilder debug = new StringBuilder();
		debug.append("\n");
		debug.append(">>>>>> ");
		debug.append("mapping = ");
		debug.append(mapping);
		debug.append("\n");
		debug.append(">>>>>> ");
		debug.append("request = ");
		debug.append(request);
		debug.append("\n");
		debug.append(">>>>>> ");
		if (fileUploads != null && !fileUploads.isEmpty()) {
			debug.append("fileUploads size = ");
			debug.append(fileUploads.size());
			fileUploads.forEach(fileUpload -> {
				debug.append("\n");
				debug.append(">>>>>>>>>>>> ");
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
		Result result = invoker.invoke(invocation);
		debug.append("\n");
		debug.append(">>>>>> ");
		debug.append("response = ");
		debug.append(result.getAttachment(Result.RESPONSE));
		LOGGER.debug(debug.toString());
		return result;
	}

}
