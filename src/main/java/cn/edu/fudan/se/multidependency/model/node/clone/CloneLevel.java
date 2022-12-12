package cn.edu.fudan.se.multidependency.model.node.clone;

import cn.edu.fudan.se.multidependency.model.node.CodeNode;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Function;
import cn.edu.fudan.se.multidependency.model.node.code.Snippet;
import cn.edu.fudan.se.multidependency.model.node.code.Type;

public enum CloneLevel {
	File(CloneLevel.FILE), Function(CloneLevel.FUNCTION), Type(CloneLevel.TYPE),
	Snippet(CloneLevel.SNIPPET), MultipleLevel(CloneLevel.MULTIPLE_LEVEL);
	
	public static CloneLevel getCodeNodeCloneLevel(CodeNode node) {
		if(node instanceof ProjectFile) {
			return File;
		} else if(node instanceof Function) {
			return Function;
		} else if(node instanceof Type) {
			return Type;
		} else if(node instanceof Snippet) {
			return Snippet;
		}
		return null;
	}

	public static final String PACKAGE = "Package";
	public static final String FILE = "File";
	public static final String FUNCTION = "Function";
	public static final String TYPE = "Type";
	public static final String SNIPPET = "Snippet";
	public static final String MULTIPLE_LEVEL = "MultipleLevel";

	private String name;

	CloneLevel(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return getName();
	}
}
