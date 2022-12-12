package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class YamlUtilsTest {

	@Test
	public void test() {
		YamlUtil.YamlObject yaml;
		try {
			yaml = YamlUtil.getDataBasePathDefault("src/main/resources/application.yml");
			String test = yaml.getForTest();
			assertTrue("this property is for YamlUtilsTest".equals(test));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
