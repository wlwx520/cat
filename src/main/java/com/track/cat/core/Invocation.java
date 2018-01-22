package com.track.cat.core;

import java.util.HashMap;
import java.util.Map;

public class Invocation {
	private Map<String, Object> attachments = new HashMap<String, Object>();
	public static final String MAPPING = "constants_mapping";
	public static final String REQUEST = "constants_request";

	@SuppressWarnings("unchecked")
	public <T> T getAttachment(String key) {
		return (T) attachments.get(key);
	}

	public <T> void setAttachment(String key, T value) {
		attachments.put(key, value);
	}

}
