package com.track.cat.core.http;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.track.cat.core.Result;

public class ResultBuilder {
	private ResultBuilder() {
	}

	private static final String RESULT = "result";
	private static final String INFO = "info";

	public static JSONObject build(int code) {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(RESULT, code);
		jsonObject.put(INFO, ErrorCode.getErrorInfo(code));
		return jsonObject;
	}

	public static JSONObject build(int code, JSONObject jsonObject) {
		jsonObject.put(RESULT, code);
		jsonObject.put(INFO, ErrorCode.getErrorInfo(code));
		return jsonObject;
	}

	public static Result buildResult(int code) {
		Result result = new Result();
		result.setAttachment(Result.RESPONSE, build(code));
		return result;
	}

	public static Result buildResult(int code, JSONObject jsonObject) {
		Result result = new Result();
		result.setAttachment(Result.RESPONSE, build(code, jsonObject));
		return result;
	}

	private static class ErrorCode {
		private static Map<Integer, String> error = new HashMap<>();

		static {
			error.put(0, "success");
		}

		public static String getErrorInfo(int code) {
			String info = error.get(code);
			return info == null ? "Unknown Error" : info;
		}
	}
}
