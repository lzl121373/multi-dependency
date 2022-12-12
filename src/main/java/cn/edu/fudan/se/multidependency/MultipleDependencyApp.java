package cn.edu.fudan.se.multidependency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication
@EnableNeo4jRepositories(basePackages = {"cn.edu.fudan.se.multidependency.repository"})
public class MultipleDependencyApp {

	private static final Logger LOGGER = LoggerFactory.getLogger(MultipleDependencyApp.class);
	
	public static void main(String[] args) {
		LOGGER.info("MultipleDependencyApp");
		if (args == null || args.length == 0) {
			SpringApplication.run(MultipleDependencyApp.class, args);
		} else {
			String operation = args[0];
			LOGGER.info(operation);
			String[] parameters = new String[args.length - 1];
			for (int i = 0; i < parameters.length; i++) {
				parameters[i] = args[i + 1];
			}
			switch (operation) {
				case "-a":
					InsertAllData.insert(parameters);
					break;
				case "-ex":
					InsertAllData.exportAllDataToFile(parameters);
					break;
				case "-im":
					InsertAllData.insertAllDataFromFile(parameters);
					break;
				case "-s":
					InsertStaticData.insert(parameters);
					break;
				case "-o":
					InsertOtherData.insert(parameters);
					break;
				case "-m":
					SpringApplication.run(MultipleDependencyApp.class, args);
					break;
				case "-p":
					try {
						cn.edu.fudan.se.multidependency.utils.FileUtil.writeToFileForProjectJSONFile(parameters);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
			}
		}
	}
//	java -jar multi-dependency-1.3.5.jar -s D:\git\multi-dependency\src\main\resources\application-fan.yml
//	java -jar multi-dependency-1.3.5.jar -o D:\git\multi-dependency\src\main\resources\application-fan.yml
//	java -jar multi-dependency-1.3.5.jar -m D:\git\multi-dependency\src\main\resources\application-fan.yml

}
