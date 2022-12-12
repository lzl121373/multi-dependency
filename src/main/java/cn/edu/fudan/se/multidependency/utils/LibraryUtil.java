package cn.edu.fudan.se.multidependency.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.fudan.se.multidependency.model.node.lib.Library;

public class LibraryUtil {

	private static final String regexLibrary = "([^_]*)__fdse__([^_]*)__fdse__[^_]*__fdse__([^_]*)__fdse__[^/].*/(.*)";
	private static final Pattern patternLibrary = Pattern.compile(regexLibrary);
	
	public static void main(String[] args) throws Exception {
		String line = "org.springframework__fdse__spring-context__fdse__0f146b__fdse__4.3.25.RELEASE__fdse__jar/spring-context-4.3.25.RELEASE.jar";
//		line = "io.jsonwebtoken__fdse__jjwt__fdse__6e87d9__fdse__0.8.0__fdse__jar/jjwt-0.8.0.jar";
		Library l = extract(line);
		System.out.println(l);
	}
	
	public static Library extract(String libLine) throws Exception {
		Matcher matcher = patternLibrary.matcher(libLine);
		if(matcher.find()) {
			Library result = new Library();
			result.setGroupId(matcher.group(1));
			result.setName(matcher.group(2));
			result.setVersion(matcher.group(3));
			result.setFullName(matcher.group(4));
			return result;
		}
		return null;
	}
	
}
