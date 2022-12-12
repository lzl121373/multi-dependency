package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class JSONUtil {
	
	public static final String SUCCESS = "success";
	public static final String FAIL = "fail";
	
	public static JSONObject extractJSONObject(File file) throws Exception {
		return JSONObject.parseObject(extractJsonString(file));
	}
	
	public static JSONArray extractJSONArray(File file) throws Exception {
		return JSONObject.parseArray(extractJsonString(file));
	}
	
	public static String extractJsonString(File file) throws Exception {
		StringBuilder builder = new StringBuilder();
		try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line = null;
			while((line = reader.readLine()) != null) {
				builder.append(line);
			}
		} 
		return builder.toString();
	}

	public static JSONObject combineJSONObjectWithoutMerge(JSONObject object1, JSONObject object2) throws JSONException {
		for(String key: object1.keySet()){
			Object value = object1.get(key);
			if(!object2.containsKey(key)){
				object2.put(key, value);
			}
		}
		return object2;
	}

}
