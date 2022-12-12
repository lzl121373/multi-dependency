package depends.extractor.java;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class InheritTypeTest extends JavaParserTest {
	@Before
	public void setUp() {
		super.init();
	}
	@Test
	public void should_handle_inherited_type_correctly() throws IOException {
        String src = "./src/test/resources/java-code-examples/InheritTest.java";
        JavaFileParser parser =createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
        assertEquals(1,entityRepo.getEntity("InheritTest").getRelations().size());
	}
}
