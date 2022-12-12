package cn.edu.fudan.se.multidependency.model.node;

public interface CodeNode extends Node {
	
	String getIdentifier();
	
	void setIdentifier(String identifier);
	
	String getIdentifierSimpleName();

    String getIdentifierSuffix();

	int getStartLine();
	
	int getEndLine();
	
	String getLanguage();
	
	default int getLines() {
		if(getStartLine() <= 0 || getEndLine() <= 0) {
			return -1;
		}
		return getEndLine() - getStartLine() + 1;
	}
	
}
