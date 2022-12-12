package cn.edu.fudan.se.multidependency.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class CppDynamicFunctionExecution extends DynamicFunctionExecution {
	public Language getLanguage() {
		return Language.cpp;
	}
	
	/// FIXME
}
