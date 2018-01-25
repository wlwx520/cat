package com.track.cat.custom.serivce;

import com.track.cat.core.HttpMethod;
import com.track.cat.core.Invocation;
import com.track.cat.core.Result;
import com.track.cat.core.annotation.AutoLifeCycle;
import com.track.cat.core.annotation.Handler;
import com.track.cat.core.annotation.Service;
import com.track.cat.core.http.ResultBuilder;
import com.track.cat.core.interfaces.IService;

@Service("/test")
public class TestService implements IService {
	@AutoLifeCycle
	private Integer a;
	@AutoLifeCycle
	private String b;
	@AutoLifeCycle
	private Long c;
	@AutoLifeCycle
	private Short d;
	@AutoLifeCycle
	private Double e;
	@AutoLifeCycle
	private Float f;
	@AutoLifeCycle
	private TestService2 test2;
	@AutoLifeCycle
	private Character g;

	@Override
	public void init() {
	}

	@Handler(value = "/text", method = { HttpMethod.POST, HttpMethod.GET })
	public Result test(Invocation invocation) {
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		System.out.println(e);
		System.out.println(f);
		System.out.println(g);
		test2.test(invocation);
		return ResultBuilder.buildResult(0);
	}

	@Handler(value = "/file", method = { HttpMethod.FILE })
	public Result test2(Invocation invocation) {
		return ResultBuilder.buildResult(0);
	}
}
