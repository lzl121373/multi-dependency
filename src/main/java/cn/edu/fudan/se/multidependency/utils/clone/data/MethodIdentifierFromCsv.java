package cn.edu.fudan.se.multidependency.utils.clone.data;

import java.util.ArrayList;
import java.util.List;

import cn.edu.fudan.se.multidependency.config.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MethodIdentifierFromCsv extends FilePathFromCsv {
	private String projectName;
	private List<String> singleIdentifiers = new ArrayList<>();
	private String functionSimpleName;
	private List<String> parameterTypes = new ArrayList<>();
	
	public String getFunctionFullName() {
		StringBuilder builder = new StringBuilder();
		builder.append(functionSimpleName);
		builder.append("(");
		builder.append(String.join(",", parameterTypes));
		builder.append(")");
		return builder.toString();
	}
	
	public String getIdentifier() {
		StringBuilder builder = new StringBuilder();
		for(String identifier : singleIdentifiers) {
			builder.append(identifier);
		}
		builder.append(getFunctionFullName());
		builder.append(Constant.CODE_NODE_IDENTIFIER_SUFFIX_FUNCTION);
		return builder.toString();
	}
	
	public void addIdentifier(String identifier) {
		this.singleIdentifiers.add(identifier);
	}
	
	public void addParameterType(String type) {
		this.parameterTypes.add(type);
	}
	
	public int countOfParameterTypes() {
		return parameterTypes.size();
	}
}
