package com.track.cat.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;

import com.track.cat.core.annotation.AutoLifeCycle;
import com.track.cat.core.exception.CatSystemException;
import com.track.cat.core.exception.ContextXmlError;
import com.track.cat.core.interfaces.IFilter;
import com.track.cat.core.interfaces.IService;

@SuppressWarnings("unchecked")
public class ApplicationContext {
	private static final Logger LOGGER = Logger.getLogger(ApplicationContext.class);
	public static final Map<Class<?>, Object> BEANS = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Element> ELEMENTS = new ConcurrentHashMap<>();

	static {
		Element beansEle = Definiens.ROOT_ELEMENT.element("beans");
		if (beansEle != null) {
			List<Element> beanList = (List<Element>) (beansEle.elements("bean"));
			if (beanList != null && !beanList.isEmpty()) {
				beanList.forEach(beanELe -> {
					String className = beanELe.attributeValue("class");
					if (className == null) {
						throw new ContextXmlError("attribute of class is not found in bean");
					}

					try {
						Class<?> clz = Class.forName(className);
						ELEMENTS.put(clz, beanELe);
					} catch (Exception e) {
						throw new ContextXmlError("class of " + className + " is not found");
					}
				});
			}
		}
	}

	private static final ApplicationContext instance = new ApplicationContext();

	private ApplicationContext() {
	}

	public static ApplicationContext instance() {
		return instance;
	}

	public <T> T getBean(Class<T> clz) {
		LOGGER.debug("get bean of " + clz.getName());
		if (BEANS.containsKey(clz)) {
			return (T) BEANS.get(clz);
		}
		LOGGER.debug("bean of " + clz.getName() + " is not found ,prepare to create");

		try {
			T newInstance = newInstance(clz);

			HashMap<String, Element> properties = getProperties(clz);

			Field[] fields = clz.getDeclaredFields();

			for (Field field : fields) {
				if (field.getAnnotation(AutoLifeCycle.class) != null) {
					if (properties == null) {
						throw new ContextXmlError(clz.getName() + " is not found in context xml");
					}
					field.setAccessible(true);
					Element propertyEle = properties.get(field.getName());
					setField(field, newInstance, propertyEle);
				}
			}

			LOGGER.debug("bean of " + clz.getName() + " created");

			BEANS.put(clz, newInstance);
			return newInstance;
		} catch (SecurityException | IllegalArgumentException e) {
			throw new CatSystemException(e);
		}
	}

	public <T extends IService> T getService(Class<T> clz) {
		LOGGER.debug("get bean of " + clz.getName());
		if (BEANS.containsKey(clz)) {
			return (T) BEANS.get(clz);
		}
		LOGGER.debug("bean of " + clz.getName() + " is not found ,prepare to create");

		try {
			T newInstance = newInstance(clz);

			HashMap<String, Element> properties = getProperties(clz);

			Field[] fields = clz.getDeclaredFields();

			for (Field field : fields) {
				if (field.getAnnotation(AutoLifeCycle.class) != null) {
					if (properties == null) {
						throw new ContextXmlError(clz.getName() + " is not found in context xml");
					}
					field.setAccessible(true);
					Element propertyEle = properties.get(field.getName());
					setField(field, newInstance, propertyEle);
				}
			}

			newInstance.init();

			LOGGER.debug("bean of " + clz.getName() + " created");

			BEANS.put(clz, newInstance);
			return newInstance;
		} catch (SecurityException | IllegalArgumentException e) {
			throw new CatSystemException(e);
		}
	}

	public <T extends IFilter> T getFilter(Class<T> clz) {
		LOGGER.debug("get bean of " + clz.getName());
		if (BEANS.containsKey(clz)) {
			return (T) BEANS.get(clz);
		}
		LOGGER.debug("bean of " + clz.getName() + " is not found ,prepare to create");

		try {
			T newInstance = newInstance(clz);

			HashMap<String, Element> properties = getProperties(clz);

			Field[] fields = clz.getDeclaredFields();

			for (Field field : fields) {
				if (field.getAnnotation(AutoLifeCycle.class) != null) {
					if (properties == null) {
						throw new ContextXmlError(clz.getName() + " is not found in context xml");
					}
					field.setAccessible(true);
					Element propertyEle = properties.get(field.getName());
					setField(field, newInstance, propertyEle);
				}
			}

			LOGGER.debug("bean of " + clz.getName() + " created");

			BEANS.put(clz, newInstance);
			return newInstance;
		} catch (SecurityException | IllegalArgumentException e) {
			throw new CatSystemException(e);
		}
	}

	private <T> void setField(Field field, T newInstance, Element propertyEle) {
		if (propertyEle.attribute("value") != null) {
			setValue(field, newInstance, propertyEle);
		} else if (propertyEle.attribute("ref") != null) {
			setRef(field, newInstance, propertyEle);
		}
	}

	private <T> void setValue(Field field, T newInstance, Element propertyEle) {
		String value = propertyEle.attributeValue("value");
		try {
			Class<?> type = field.getType();
			if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
				field.set(newInstance, Integer.valueOf(value));
			} else if (type.isAssignableFrom(Character.class) || type.isAssignableFrom(char.class)) {
				if (value.length() != 1) {
					throw new ContextXmlError(field.getName() + " is not a character");
				}
				field.set(newInstance, value.charAt(0));
			} else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {
				field.set(newInstance, Boolean.valueOf(value));
			} else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
				field.set(newInstance, Long.valueOf(value));
			} else if (type.isAssignableFrom(Short.class) || type.isAssignableFrom(short.class)) {
				field.set(newInstance, Short.valueOf(value));
			} else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
				field.set(newInstance, Float.valueOf(value));
			} else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
				field.set(newInstance, Double.valueOf(value));
			} else if (type.isAssignableFrom(String.class)) {
				field.set(newInstance, value);
			} else {
				throw new ContextXmlError("type of " + field.getName() + " is not support");
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private <T> void setRef(Field field, T newInstance, Element propertyEle) {
		String className = propertyEle.attributeValue("ref");
		try {
			Class<?> clz = (Class<?>) Class.forName(className);
			field.set(newInstance, getBean(clz));
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException e) {
			throw new ContextXmlError(className + " is not found");
		}
	}

	private <T> T newInstance(Class<T> clz) {
		try {
			Constructor<T> constructor = clz.getConstructor();
			constructor.setAccessible(true);
			T newInstance = constructor.newInstance();
			return newInstance;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new CatSystemException(e);
		}
	}

	private <T> HashMap<String, Element> getProperties(Class<T> clz) {
		Element element = ELEMENTS.get(clz);
		if (element == null) {
			return null;
		}
		List<Element> propertyELes = element.elements("property");
		HashMap<String, Element> propertyELeMap = new HashMap<>();
		for (Element property : propertyELes) {
			Attribute key = property.attribute("key");
			if (key == null) {
				throw new CatSystemException("key is not found in property , class = " + clz.getName());
			}
			propertyELeMap.put(key.getData().toString(), property);
		}
		return propertyELeMap;
	}

}
