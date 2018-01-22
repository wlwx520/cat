package com.track.cat.custom.serivce;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.HttpMethod;
import com.track.cat.core.handler.annotation.Handler;
import com.track.cat.core.handler.annotation.Service;
import com.track.cat.core.handler.interfaces.IService;
import com.track.cat.core.http.ResultBuilder;

@Service("/test")
public class TestService implements IService {

	@Handler(value = "/text", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}

	@Handler(value = "/file", method = { HttpMethod.FILE })
	public Result test2(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}
}
