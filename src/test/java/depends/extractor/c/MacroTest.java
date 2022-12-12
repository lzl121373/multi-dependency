package depends.extractor.c;

import depends.extractor.cpp.CppFileParser;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

public class MacroTest extends CParserTest{
    @Before
    public void setUp() {
        super.init();
    }

    @Test
    public void macro_define() throws IOException {
        String[] srcs = new String[] {
                "./src/test/resources/c-code-examples/index.c",
        };

        for (String src:srcs) {
            CppFileParser parser = createParser(src);
            parser.parse();
        }
        inferer.resolveAllBindings();

        assertNotNull(this.repo.getEntity("TAD_MIN_CHUNK_GAP"));
    }
}



