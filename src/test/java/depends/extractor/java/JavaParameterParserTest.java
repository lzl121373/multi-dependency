package depends.extractor.java;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JavaParameterParserTest extends JavaParserTest {
	@Before
	public void setUp() {
		super.init();
	}
	
	@Test
	public void test_parameter() throws IOException {
        String src = "./src/test/resources/java-code-examples/FunctionParameters.java";
        JavaFileParser parser = createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
        assertEquals(4,entityRepo.getEntity("FunctionParameters.function_with_parameters_same_type").getRelations().size());
	}

}
