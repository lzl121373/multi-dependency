package cn.edu.fudan.se.multidependency.model.relation;

import org.neo4j.graphdb.RelationshipType;

import java.util.EnumMap;
import java.util.Map;

public enum RelationType implements RelationshipType {
	CONTAIN(RelationType.str_CONTAIN),
	HAS(RelationType.str_HAS),
	RELATE_TO(RelationType.str_RELATE_TO),
	EXTENDS(RelationType.str_EXTENDS),
	IMPORT(RelationType.str_IMPORT),
	INCLUDE(RelationType.str_INCLUDE),
	ACCESS(RelationType.str_ACCESS),
	CALL(RelationType.str_CALL),
	CREATE(RelationType.str_CREATE),
	IMPLEMENTS(RelationType.str_IMPLEMENTS),
	IMPLEMENTS_C(RelationType.str_IMPLEMENTS_C),
	IMPLLINK(RelationType.str_IMPLLINK),
	PARAMETER(RelationType.str_PARAMETER),
	GENERIC_PARAMETER(RelationType.str_GENERIC_PARAMETER),
	RETURN(RelationType.str_RETURN),
	THROW(RelationType.str_THROW),
	CAST(RelationType.str_CAST),
	VARIABLE_TYPE(RelationType.str_VARIABLE_TYPE),
	ANNOTATION(RelationType.str_ANNOTATION),
//	DEPENDENCY(RelationType.str_DEPENDENCY),
	GLOBAL_VARIABLE(RelationType.str_GLOBAL_VARIABLE),
	MEMBER_VARIABLE(RelationType.str_MEMBER_VARIABLE),
	LOCAL_VARIABLE(RelationType.str_LOCAL_VARIABLE),
	USE(RelationType.str_USE),
	DYNAMIC_CALL(RelationType.str_DYNAMIC_CALL),
	TESTCASE_EXECUTE_FEATURE(RelationType.str_TESTCASE_EXECUTE_FEATURE),
	TESTCASE_RUN_TRACE(RelationType.str_TESTCASE_RUN_TRACE),
	SCENARIO_DEFINE_TESTCASE(RelationType.str_SCENARIO_DEFINE_TESTCASE),
	MICRO_SERVICE_CREATE_SPAN(RelationType.str_MICRO_SERVICE_CREATE_SPAN),
	SPAN_CALL_SPAN(RelationType.str_SPAN_CALL_SPAN),
	MICROSERVICE_CALL_MICROSERVICE(RelationType.str_MICROSERVICE_CALL_MICROSERVICE),
	SPAN_START_WITH_FUNCTION(RelationType.str_SPAN_START_WITH_FUNCTION),
	TRACE_RUN_WITH_FUNCTION(RelationType.str_TRACE_RUN_WITH_FUNCTION),
	FILE_BUILD_DEPENDS_FILE(RelationType.str_FILE_BUILD_DEPENDS_FILE),
	SPAN_INSTANCE_OF_RESTFUL_API(RelationType.str_SPAN_INSTANCE_OF_RESTFUL_API),
	MICROSERVICE_DEPEND_ON_MICROSERVICE(RelationType.str_MICROSERVICE_DEPEND_ON_MICROSERVICE),
	COMMIT_UPDATE_FILE(RelationType.str_COMMIT_UPDATE_FILE),
	COMMIT_ADDRESS_ISSUE(RelationType.str_COMMIT_ADDRESS_ISSUE),
	DEVELOPER_REPORT_ISSUE(RelationType.str_DEVELOPER_REPORT_ISSUE),
	DEVELOPER_SUBMIT_COMMIT(RelationType.str_DEVELOPER_SUBMIT_COMMIT),
	COMMIT_INHERIT_COMMIT(RelationType.str_COMMIT_INHERIT_COMMIT),
	CO_CHANGE(RelationType.str_CO_CHANGE),
	FUNCTION_CALL_LIBRARY_API(RelationType.str_FUNCTION_CALL_LIBRARY_API),
	CLONE(RelationType.str_CLONE),
	DEPENDS_ON(RelationType.str_DEPENDS_ON),
	DEPENDS(RelationType.str_DEPENDS),
	MODULE_CLONE(RelationType.str_MODULE_CLONE),
	AGGREGATION_CLONE(RelationType.str_AGGREGATION_CLONE),
	AGGREGATION_DEPENDS_ON(RelationType.str_AGGREGATION_DEPENDS_ON),
	AGGREGATION_CO_CHANGE(RelationType.str_AGGREGATION_CO_CHANGE),
	CO_DEVELOPER(RelationType.str_CO_DEVELOPER),
	COUPLING(RelationType.str_COUPLING);

	/**
	 * 结构关系
	 */
	public static final String str_CONTAIN = "CONTAIN";
	public static final String str_HAS = "HAS";
	public static final String str_RELATE_TO = "RELATE_TO";
//	public static final String str_DEPENDENCY = "DEPENDENCY";
//	public static final String str_ASSOCIATION = "ASSOCIATION";
	public static final String str_GLOBAL_VARIABLE = "GLOBAL_VARIABLE";
	public static final String str_MEMBER_VARIABLE = "MEMBER_VARIABLE";

	/**
	 * 依赖关系
	 */
	public static final String str_EXTENDS = "EXTENDS";
	public static final String str_IMPORT = "IMPORT";
	public static final String str_INCLUDE = "INCLUDE";
	public static final String str_ACCESS = "ACCESS";
	public static final String str_USE = "USE";
	public static final String str_LOCAL_VARIABLE = "LOCAL_VARIABLE";
	public static final String str_CALL = "CALL";
	public static final String str_CREATE = "CREATE";
	public static final String str_IMPLEMENTS = "IMPLEMENTS";
	public static final String str_IMPLEMENTS_C = "IMPLEMENTS_C";
	public static final String str_IMPLLINK = "IMPLLINK";
	public static final String str_PARAMETER = "PARAMETER";
	public static final String str_GENERIC_PARAMETER = "GENERIC_PARAMETER";
	public static final String str_CAST = "CAST";
	public static final String str_RETURN = "RETURN";
	public static final String str_VARIABLE_TYPE = "VARIABLE_TYPE";
	public static final String str_DYNAMIC_CALL = "DYNAMIC_CALL";
	public static final String str_THROW = "THROW";
	public static final String str_ANNOTATION = "ANNOTATION";
	public static final String str_TESTCASE_EXECUTE_FEATURE = "TESTCASE_EXECUTE_FEATURE";
	public static final String str_SCENARIO_DEFINE_TESTCASE = "SCENARIO_DEFINE_TESTCASE";
	public static final String str_SPAN_CALL_SPAN = "SPAN_CALL_SPAN";
	public static final String str_SPAN_INSTANCE_OF_RESTFUL_API = "SPAN_INSTANCE_OF_RESTFUL_API";
	public static final String str_MICRO_SERVICE_CREATE_SPAN = "MICRO_SERVICE_CREATE_SPAN";
	public static final String str_SPAN_START_WITH_FUNCTION = "SPAN_START_WITH_FUNCTION";
	public static final String str_TESTCASE_RUN_TRACE = "TESTCASE_RUN_TRACE";
	public static final String str_TRACE_RUN_WITH_FUNCTION = "TRACE_RUN_WITH_FUNCTION";
	public static final String str_FILE_BUILD_DEPENDS_FILE = "FILE_BUILD_DEPENDS_FILE";
	public static final String str_MICROSERVICE_CALL_MICROSERVICE = "MICROSERVICE_CALL_MICROSERVICE";
	public static final String str_MICROSERVICE_DEPEND_ON_MICROSERVICE = "MICROSERVICE_DEPEND_ON_MICROSERVICE";
	public static final String str_COMMIT_UPDATE_FILE = "COMMIT_UPDATE_FILE";
	public static final String str_COMMIT_ADDRESS_ISSUE = "COMMIT_ADDRESS_ISSUE";
	public static final String str_DEVELOPER_REPORT_ISSUE = "DEVELOPER_REPORT_ISSUE";
	public static final String str_DEVELOPER_SUBMIT_COMMIT = "DEVELOPER_SUBMIT_COMMIT";
	public static final String str_COMMIT_INHERIT_COMMIT = "COMMIT_INHERIT_COMMIT";
	public static final String str_CO_CHANGE = "CO_CHANGE";
	public static final String str_FUNCTION_CALL_LIBRARY_API = "FUNCTION_CALL_LIBRARY_API";
	public static final String str_CLONE = "CLONE";
	public static final String str_CO_DEVELOPER = "CO_DEVELOPER";
	/**
	 * 聚合关系
	 */
	public static final String str_DEPENDS_ON = "DEPENDS_ON";
	public static final String str_DEPENDS = "DEPENDS";
	public static final String str_MODULE_CLONE = "MODULE_CLONE";
	public static final String str_AGGREGATION_CLONE = "AGGREGATION_CLONE";
	public static final String str_AGGREGATION_DEPENDS_ON = "AGGREGATION_DEPENDS_ON";
	public static final String str_AGGREGATION_CO_CHANGE = "AGGREGATION_CO_CHANGE";

	/**
	 * 耦合关系
	 */
	public static final String str_COUPLING = "COUPLING";

	/**
	 * 关系权重
	 */
	public static Map<RelationType, Double> relationWeights = new EnumMap<>(RelationType.class);

	/**
	 * 关系简称
	 */
	public static Map<RelationType, String> relationAbbreviation = new EnumMap<>(RelationType.class);


	private String name;

	RelationType(String name) {
		this.name = name;
	}

	static{
		relationWeights.put(IMPORT, 0.1);
		relationWeights.put(INCLUDE, 0.1);
		relationWeights.put(EXTENDS, 0.9);
		relationWeights.put(IMPLEMENTS, 1.0);
		relationWeights.put(MEMBER_VARIABLE, 0.1);
		relationWeights.put(GLOBAL_VARIABLE, 0.1);
		relationWeights.put(LOCAL_VARIABLE, 0.1);
		relationWeights.put(ANNOTATION, 0.1);
		relationWeights.put(CALL, 0.1);
		relationWeights.put(CAST, 0.1);
		relationWeights.put(CREATE, 0.1);
		relationWeights.put(USE, 0.1);
		relationWeights.put(PARAMETER, 0.1);
		relationWeights.put(THROW, 0.1);
		relationWeights.put(RETURN, 0.1);
		relationWeights.put(IMPLLINK, 0.1);
		relationWeights.put(VARIABLE_TYPE, 0.1);
		relationWeights.put(IMPLEMENTS_C, 0.1);
//		relationWeights.put(DEPENDENCY, 0.1);
		relationWeights.put(CO_CHANGE, 0.1);
		relationWeights.put(CLONE, 0.1);
	}

	static{
		relationAbbreviation.put(IMPORT, "IMPO");
		relationAbbreviation.put(INCLUDE, "INC");
		relationAbbreviation.put(EXTENDS, "EXT");
		relationAbbreviation.put(IMPLEMENTS, "IMPL");
		relationAbbreviation.put(GLOBAL_VARIABLE, "GVAR");
		relationAbbreviation.put(MEMBER_VARIABLE, "MVAR");
		relationAbbreviation.put(LOCAL_VARIABLE, "LVAR");
		relationAbbreviation.put(ANNOTATION, "ANN");
		relationAbbreviation.put(CALL, "CAL");
		relationAbbreviation.put(CAST, "CAS");
		relationAbbreviation.put(CREATE, "CRE");
		relationAbbreviation.put(USE, "USE");
		relationAbbreviation.put(PARAMETER, "PAR");
		relationAbbreviation.put(GENERIC_PARAMETER, "GPAR");
		relationAbbreviation.put(THROW, "THR");
		relationAbbreviation.put(RETURN, "RET");
		relationAbbreviation.put(IMPLLINK, "IMLK");
		relationAbbreviation.put(VARIABLE_TYPE, "VART");
		relationAbbreviation.put(IMPLEMENTS_C, "IMPLC");
//		relationAbbreviation.put(DEPENDENCY, "DEP");
		relationAbbreviation.put(CO_CHANGE, "COC");
		relationAbbreviation.put(CLONE, "CLO");
	}

	public static void setRelationWeight(RelationType relationType, Double weight){
		relationWeights.put(relationType, weight);
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return getName();
	}
}
