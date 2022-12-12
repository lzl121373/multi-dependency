package depends.extractor.cpp;

import depends.deptypes.DependencyType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class TypeDefTest extends CppParserTest {
    @Before
    public void setUp() {
    	super.init();
    }
	
	@Test
	public void test_ref_parameter() throws IOException {
	    String[] srcs = new String[] {
	    		"./src/test/resources/cpp-code-examples/TypeDefTest.cpp",
	    	    };
	    
	    for (String src:srcs) {
		    CppFileParser parser = createParser(src);
		    parser.parse();
	    }
	    inferer.resolveAllBindings();
        this.assertContainsRelation(repo.getEntity("foo"), DependencyType.PARAMETER, "MyInt");
	}
			
}