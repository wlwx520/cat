package com.track.cat.custom.serivce;

import java.util.Map;

import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.handler.HttpMethod;
import com.track.cat.core.handler.annotation.Handler;
import com.track.cat.core.handler.annotation.Service;
import com.track.cat.core.handler.interfaces.IService;
import com.track.cat.core.http.ResultBuilder;

@Service("/test")
public class TestService implements IService {
	@Handler(value = "/aaa", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		Map<String, String> data = invocation.getAttachment(Invocation.REQUEST);
		System.out.println("in -----> " + data.toString());
		return ResultBuilder.buildResult(0);
	}
}
