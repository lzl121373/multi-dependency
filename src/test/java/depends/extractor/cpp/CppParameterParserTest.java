package depends.extractor.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CppParameterParserTest extends CppParserTest{
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void test_parameter() throws IOException {
	    String src = "./src/test/resources/cpp-code-examples/FunctionParameters.cpp";
	    CppFileParser parser = createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
        assertEquals(4,repo.getEntity("FunctionParameters.function_with_parameters_same_type").getRelations().size());
	}

}
