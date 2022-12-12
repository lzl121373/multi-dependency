package depends.extractor.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ConstructFunctionReturnValueTest extends CppParserTest{
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void test_constructor() throws IOException {
	    String[] srcs = new String[] {
	    		"./src/test/resources/cpp-code-examples/ConstructFunction.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
        //assertEquals(1,repo.getEntity("UnderTest").getRelations().size());
	}
	
	@Test
	public void test_empty_contains() throws IOException {
	    String[] srcs = new String[] {
	    		"./src/test/resources/cpp-code-examples/EmptyContains.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
        //assertEquals(1,repo.getEntity("UnderTest").getRelations().size());
	}
		
}