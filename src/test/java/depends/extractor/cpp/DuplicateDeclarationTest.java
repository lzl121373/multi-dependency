package depends.extractor.cpp;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class DuplicateDeclarationTest extends CppParserTest {
	 @Before
	    public void setUp() {
	    	super.init();
	    }
		
		@Test
		public void duplication_declaration_should_be_resolved() throws IOException {
			
			 String[] srcs = new String[] {
			    		"./src/test/resources/cpp-code-examples/DuplicationDeclarationCouldBeResolved.cpp",
			    	    };
			    
			    for (String src:srcs) {
				    CppFileParser parser = createParser(src);
				    parser.parse();
			    }
			    inferer.resolveAllBindings();
		        assertEquals(6,repo.getEntity("X.invoke").getRelations().size());
		}


}
