package cn.edu.fudan.se.multidependency.model.node.smell;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.ar.Module;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public final class SmellLevel {

	public static String getNodeSmellLevel(Node node) {
		if(node instanceof ProjectFile) {
			return FILE;
		} else if(node instanceof Function) {
			return FUNCTION;
		} else if(node instanceof Type) {
			return TYPE;
		} else if(node instanceof Snippet) {
			return SNIPPET;
		}else if(node instanceof Package) {
			return PACKAGE;
		}else if(node instanceof Module) {
			return MODULE;
		}
		return null;
	}

	public static final String MODULE = "Module";
	public static final String PACKAGE = "Package";
	public static final String FILE = "File";
	public static final String FUNCTION = "Function";
	public static final String TYPE = "Type";
	public static final String SNIPPET = "Snippet";
	public static final String MULTIPLE_LEVEL = "MultipleLevel";

}
