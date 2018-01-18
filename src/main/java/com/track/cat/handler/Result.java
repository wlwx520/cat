package com.track.cat.handler;

import com.alibaba.fastjson.JSONObject;

public class Result {
	private JSONObject result;

	public Result() {
		this.result = new JSONObject();
	}

	public void setAttachment(String key, Object value) {
		result.put(key, value);
	}

	public Object getAttachment(String key) {
		return result.get(key);
	}

	public JSONObject getResult() {
		return result;
	}
}
