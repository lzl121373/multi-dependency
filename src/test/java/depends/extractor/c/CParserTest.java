package depends.extractor.c;

import depends.entity.repo.EntityRepo;
import depends.entity.repo.InMemoryEntityRepo;
import depends.extractor.ParserTest;
import depends.extractor.cpp.*;
import depends.extractor.cpp.cdt.CdtCppFileParser;
import depends.extractor.cpp.cdt.PreprocessorHandler;
import depends.relations.Inferer;
import multilang.depends.util.file.TemporaryFile;

import java.util.ArrayList;

public abstract class CParserTest extends ParserTest {
    protected EntityRepo repo;
    protected Inferer inferer;
    protected PreprocessorHandler preprocessorHandler;
    private MacroRepo macroRepo;

    public void init() {
        repo = new InMemoryEntityRepo();
        inferer = new Inferer(repo,new CppImportLookupStrategy(),new CppBuiltInType(),false);
        preprocessorHandler = new PreprocessorHandler("./src/test/resources/c-code-examples/",new ArrayList<>(), new ArrayList<>());
        TemporaryFile.reset();
//    	macroRepo = new MacroMemoryRepo();
    	macroRepo = new MacroFileRepo(repo);
//        macroRepo = new MacroEhcacheRepo(repo);

    }

    public CppFileParser createParser(String src) {
        return new CdtCppFileParser(src,repo, preprocessorHandler,inferer,macroRepo );
    }
}
