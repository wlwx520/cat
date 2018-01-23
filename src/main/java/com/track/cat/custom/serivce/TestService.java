package com.track.cat.custom.serivce;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.annotation.AutoLifeCycle;
import com.track.cat.core.annotation.Handler;
import com.track.cat.core.annotation.Service;
import com.track.cat.core.handler.HttpMethod;
import com.track.cat.core.http.ResultBuilder;
import com.track.cat.core.interfaces.IService;
import com.track.cat.custom.filter.LogDebugFilter;

@Service("/test")
public class TestService implements IService {
	@AutoLifeCycle
	private LogDebugFilter filter;

	@Handler(value = "/text", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}

	@Handler(value = "/file", method = { HttpMethod.FILE })
	public Result test2(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}
}
