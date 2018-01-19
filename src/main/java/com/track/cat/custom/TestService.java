package com.track.cat.custom;

import com.alibaba.fastjson.JSONObject;
import com.track.cat.handler.Invocation;
import com.track.cat.handler.Result;
import com.track.cat.handler.annotation.Handler;
import com.track.cat.handler.annotation.Service;
import com.track.cat.http.ResultBuilder;

@Service("/test")
public class TestService {
	@Handler("/aaa")
	public Result test(Invocation invocation) {
		JSONObject attachment = (JSONObject) invocation.getAttachment(Invocation.JSON_REQEST);
		System.out.println("in -----> " + attachment.toString());
		return ResultBuilder.buildResult(0);
	}
}
