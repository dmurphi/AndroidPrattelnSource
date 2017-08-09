package com.cityparking.pratteln.utils;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonUtil {

	private static JsonUtil instance;

	private Gson gson = null;

	private JsonUtil() {
		gson = new Gson();
	}

	public static <T> T jsonToObject(String json, Class<T> toClass) throws Exception {
		if (instance == null) {
			instance = new JsonUtil();
		}
		return instance.gson.fromJson(json, toClass);
	}

	public static <T> T jsonToObject(String json, Type toClass) throws Exception {
		if (instance == null) {
			instance = new JsonUtil();
		}
		return instance.gson.fromJson(json, toClass);
	}

	public static <T> T jsonToObject(InputStream databaseInputStream, Class<T> toClass) throws Exception {
		if (instance == null) {
			instance = new JsonUtil();
		}
		return instance.gson.fromJson(new InputStreamReader(databaseInputStream), toClass);
	}

	public static <T> String objectToJson(Object o, Class<T> toClass) throws Exception {
		if (instance == null) {
			instance = new JsonUtil();
		}
		return instance.gson.toJson(o, toClass);
	}
}