package com.track.cat.custom.serivce;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.annotation.Handler;
import com.track.cat.core.annotation.Service;
import com.track.cat.core.handler.HttpMethod;
import com.track.cat.core.http.ResultBuilder;
import com.track.cat.core.interfaces.IService;

@Service("/test2")
public class TestService2 implements IService {
	
	@Override
	public void init() {

	}

	@Handler(value = "/text", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		System.out.println("test 222222222222");
		return ResultBuilder.buildResult(0);
	}

	@Handler(value = "/file", method = { HttpMethod.FILE })
	public Result test2(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}

}
