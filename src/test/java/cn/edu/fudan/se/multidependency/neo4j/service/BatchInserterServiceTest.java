package cn.edu.fudan.se.multidependency.neo4j.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Type;
import cn.edu.fudan.se.multidependency.model.node.code.Variable;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.service.insert.BatchInserterService;
import cn.edu.fudan.se.multidependency.utils.YamlUtil;

public class BatchInserterServiceTest {

	@Test
	public void test() {
		YamlUtil.YamlObject yaml = null;
		try {
			yaml = YamlUtil.getDataBasePathDefault("src/main/resources/application.yml");
			String test = yaml.getForTest();
			assertTrue("this property is for YamlUtilsTest".equals(test));
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*try(BatchInserterService inserter = BatchInserterService.getInstance();){
			inserter.init(yaml.getNeo4jDatabasePath(), true);
			
			Package pck = new Package();
			pck.setPackageName("test");
			inserter.insertNode(pck);
			
			ProjectFile file = new ProjectFile();
			file.setFileName("src/test/Test.java");
			file.setPath("src/test/Test.java");
			inserter.insertNode(file);
			
			Type type = new Type();
			type.setTypeName("test.Test");
			inserter.insertNode(type);
			
			Function function = new Function();
			function.setFunctionName("test.Test.test");
			inserter.insertNode(function);
			
			Variable variable = new Variable();
			variable.setVariableName("test.Test.vtest");
			inserter.insertNode(variable);
			
			Contain packageContainsFile = new Contain(pck, file);
			inserter.insertRelation(packageContainsFile);
			
			Contain fileContainsType = new Contain(file, type);
			inserter.insertRelation(fileContainsType);
			
			Contain typeContainsFunction = new Contain(type, function);
			inserter.insertRelation(typeContainsFunction);
			
			Contain typeContainsVariable = new Contain(type, variable);
			inserter.insertRelation(typeContainsVariable);
			
			List<Long> ids = inserter.getRelationshipIds(type.getId());
			assertTrue(ids.size() == 3);
			
			Map<String, Object> properties = inserter.getNodeProperties(type.getId());
			assertEquals(properties.get("typeName"), "test.Test");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}*/
		
	}

}
