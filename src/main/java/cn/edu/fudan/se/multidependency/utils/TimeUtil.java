package cn.edu.fudan.se.multidependency.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class TimeUtil {

	public static Long changeTimeStrToLong(String time) {
		SimpleDateFormat sim = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
		Long result = -1L;
		try {
			result = sim.parse(time).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}		
		return result;
	}
}
