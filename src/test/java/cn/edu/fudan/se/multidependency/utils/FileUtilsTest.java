package cn.edu.fudan.se.multidependency.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtilsTest {

//	@Test
	public void test() {
		String projectPath = "/bash-5.0";
		String filePath = "D:\\multiple-dependency-project\\bash-5.0\\lib\\malloc\\malloc.c";
		String directoryPath = "D:\\multiple-dependency-project\\bash-5.0\\lib\\malloc";
		filePath = filePath.replace("\\", "/");
		filePath = filePath.substring(filePath.indexOf(projectPath + "/"));
		directoryPath = directoryPath.replace("\\", "/");
		directoryPath = directoryPath.substring(directoryPath.indexOf(projectPath + "/"));
		assertEquals(filePath, "/bash-5.0/lib/malloc/malloc.c");
		assertEquals(directoryPath, "/bash-5.0/lib/malloc");
	}
	
//	@Test
	public void testListDirectory() {
		File directory = new File("src/main/resources/dynamic");
		List<File> files = new ArrayList<>();
		FileUtil.listDirectories(directory, 0, files);
		System.out.println(files.size());
		assertEquals(1, files.size());
		files.clear();
		System.out.println(files.size());
		FileUtil.listDirectories(directory, 1, files);
		System.out.println(files.size());
		assertEquals(2, files.size());
		
	}
	
}
