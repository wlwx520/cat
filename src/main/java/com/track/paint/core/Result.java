package com.track.paint.core;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

public class Result {
	public static final String RESPONSE = "constants_response";
	public static final String DOWNLOAD = "constants_download";
	public static final String DOWNLOAD_NAME = "constants_download_name";
	private Map<String, Object> attachments = new HashMap<String, Object>();

	public Object getAttachment(String key) {
		return attachments.get(key);
	}

	public void setAttachment(String key, Object value) {
		attachments.put(key, value);
	}

	public static JSONObject decode(Result result) {
		return (JSONObject) result.getAttachment(RESPONSE);
	}

}
