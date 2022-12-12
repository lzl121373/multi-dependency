package depends.extractor.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ParseErrorTest extends CppParserTest {
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void full_qualified_names_should_be_resolved() throws IOException {
	    String[] srcs = new String[] {
	    		"./src/test/resources/cpp-code-examples/parseError/error1.c",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
	}
	
}
