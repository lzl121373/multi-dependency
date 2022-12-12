package depends.extractor.cpp;

import depends.entity.MultiDeclareEntities;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class MacroTest extends CppParserTest{
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void macro_should_be_expanded() throws IOException {
	    String[] srcs = new String[] {
	    		 "./src/test/resources/cpp-code-examples/macros/Macro.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
	    
        assertNotNull(this.repo.getEntity("Macro"));
	}

	@Test
	public void header_file_can_only_be_count_once() throws IOException {
	    String[] srcs = new String[] {
	    		 "./src/test/resources/cpp-code-examples/macros/Macro.cpp",
	    		 "./src/test/resources/cpp-code-examples/macros/Macro2.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
	    
        assertFalse(repo.getEntity("foo") instanceof MultiDeclareEntities);
	}
	
	
	@Test
	public void intermediate_file_include_should_work() throws IOException {
	    String[] srcs = new String[] {
	    		 "./src/test/resources/cpp-code-examples/macros/Macro3.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
	    
        assertNotNull(repo.getEntity("Macro3.bar"));
	}
	
	

}
