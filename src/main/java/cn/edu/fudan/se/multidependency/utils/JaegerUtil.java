package cn.edu.fudan.se.multidependency.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.alibaba.fastjson.JSONObject;

public class JaegerUtil {
	
	public static JSONObject readJSON(String traceId) throws Exception {
		JSONObject result = new JSONObject();
		URL url = new URL("http://10.141.221.74:16686/api/traces/" + traceId);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.connect();
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"utf-8"));//转成utf-8格式
		String line;
		while((line=reader.readLine())!=null){
			builder.append(line);
		}
		connection.disconnect();
		result = JSONObject.parseObject(builder.toString());
		return result;
	}
	
}
