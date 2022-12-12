package cn.edu.fudan.se.multidependency.config;

import java.util.HashSet;
import java.util.Set;

public class Constant {
	
	public static final String TIMESTAMP = "yyyy-MM-dd HH:mm:ss";

	public static final int SIZE_OF_PAGE = 15;
	
	public static final int COUNT_OF_MIN_COCHANGE = 3;
	public static final double MIN_CHANGE_CONFIDENCE = 0.3;

	public static final String ISSUE_FROM_JIRA = "jira";
	public static final String ISSUE_FROM_GITHUB = "github";
	public static final String DEVELOPER_ROLE_CODER = "coder";
	public static final String DEVELOPER_ROLE_ISSUE_REPORTER = "issue_reporter";

	public static final String[] FILE_SUFFIX =
			new String[]{".java",".cpp", ".cc", ".c", ".c++", ".h", ".hpp", ".hh", ".cxx", ".hxx"};

	public static final String PROJECT_STRUCTURE_TREEMAP = "PROJECT_STRUCTURE_TREEMAP";
	public static final String PROJECT_STRUCTURE_CIRCLE_PACKING = "PROJECT_STRUCTURE_CIRCLE_PACKING";
	public static final String PROJECT_STRUCTURE_COMBO = "PROJECT_STRUCTURE_COMBO";

    public static final String CODE_NODE_IDENTIFIER_SUFFIX_FILE = "#F";
    public static final String CODE_NODE_IDENTIFIER_SUFFIX_NAMESPACE = "#N";
    public static final String CODE_NODE_IDENTIFIER_SUFFIX_TYPE = "#T";
    public static final String CODE_NODE_IDENTIFIER_SUFFIX_FUNCTION = "#M";
    public static final String CODE_NODE_IDENTIFIER_SUFFIX_VARIABLE = "#V";
    public static final String CODE_NODE_IDENTIFIER_SUFFIX_SNIPPET = "#S";

    private static final Set<String> CODE_NODE_IDENTIFIER_SUFFIXs = new HashSet<>();
    
    static {
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_FILE);
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_NAMESPACE);
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_TYPE);
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_FUNCTION);
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_VARIABLE);
    	CODE_NODE_IDENTIFIER_SUFFIXs.add(CODE_NODE_IDENTIFIER_SUFFIX_SNIPPET);
    }

	public static String isEndWithCodeNodeIdentifierSuffix(String str) {
		for(String suffix : CODE_NODE_IDENTIFIER_SUFFIXs) {
			if(str.endsWith(suffix)) {
				return suffix;
			}
		}
		return null;
	}
}
