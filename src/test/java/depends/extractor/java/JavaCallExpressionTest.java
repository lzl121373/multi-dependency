package depends.extractor.java;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class JavaCallExpressionTest extends JavaParserTest {
	@Before
	public void setUp() {
		super.init();
	}
	@Test
	public void test() throws IOException {
        String src = "./src/test/resources/java-code-examples/SimpleExpressionCallTest.java";
        JavaFileParser parser = createParser(src);
        parser.parse();
        inferer.resolveAllBindings();
	}

}
