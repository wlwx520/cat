package com.track.paint.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.track.paint.core.HttpMethod;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Handler {
	public String value();

	public HttpMethod[] method() default { HttpMethod.GET };

}
