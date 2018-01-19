package com.track.cat.handler;

import java.util.HashMap;
import java.util.Map;

public class Invocation {
	private Map<String, Object> attachments = new HashMap<String, Object>();
	public static final String MAPPING = "constants_mapping";
	public static final String JSON_REQEST = "constants_json_request";

	public Object getAttachment(String key) {
		return attachments.get(key);
	}

	public void setAttachment(String key, Object value) {
		attachments.put(key, value);
	}

}
