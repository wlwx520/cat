package com.track.cat.core;

import java.util.HashMap;
import java.util.Map;

public class Result {
	public static final String RESPONSE = "constants_response";
	private Map<String, Object> attachments = new HashMap<String, Object>();

	public Object getAttachment(String key) {
		return attachments.get(key);
	}

	public void setAttachment(String key, Object value) {
		attachments.put(key, value);
	}

}