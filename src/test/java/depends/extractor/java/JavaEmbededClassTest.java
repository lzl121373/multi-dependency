package depends.extractor.java;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JavaEmbededClassTest extends JavaParserTest{
	@Before
	public void setUp() {
		super.init();
	}
	
	@Test
	public void test_EmbededTypeWithImport() throws IOException {
        String src = "./src/test/resources/java-code-examples/EmbededTest.java";
        JavaFileParser parser = createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
        assertEquals(1,entityRepo.getEntity("x.EmbededTest").getRelations().size());
	}

	@Test
	public void test_EmbededTypeWithoutImport() throws IOException {
        String src = "./src/test/resources/java-code-examples/EmbededTest.java";
        JavaFileParser parser = createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
        assertEquals(1,entityRepo.getEntity("x.EmbededTest2").getRelations().size());
	}
}
