package depends.extractor.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ForwardDeclareTest extends CppParserTest {
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void should_slove_forward_declare_in_cpp() throws IOException {
	    String[] srcs = new String[] {
	    		"./src/test/resources/cpp-code-examples/forwardDeclare/App.cpp",
	    		"./src/test/resources/cpp-code-examples/forwardDeclare/Mutex.h",
	    		"./src/test/resources/cpp-code-examples/forwardDeclare/App.h",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
        //TODO: to be complete
	}
	
}
