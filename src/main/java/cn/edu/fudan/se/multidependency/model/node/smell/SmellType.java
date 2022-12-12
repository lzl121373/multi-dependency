package cn.edu.fudan.se.multidependency.model.node.smell;

public final class SmellType {

	public static final String CLONE = "Clone";

	public static final String CYCLIC_DEPENDENCY = "CyclicDependency";
	public static final String HUBLIKE_DEPENDENCY = "HubLikeDependency";
	public static final String UNSTABLE_DEPENDENCY = "UnstableDependency";
	public static final String UNSTABLE_INTERFACE = "UnstableInterface";
	public static final String LOGICAL_COUPLING = "LogicalCoupling";
	public static final String SIMILAR_COMPONENTS = "SimilarComponents";
	public static final String UNUSED_COMPONENT = "UnusedComponent";
	public static final String IMPLICIT_CROSS_MODULE_DEPENDENCY = "ImplicitCrossModuleDependency";
	public static final String GOD_COMPONENT = "GodComponent";
	public static final String UNUTILIZED_ABSTRACTION = "UnutilizedAbstraction";
	public static final String MULTIPLE_SMELLS = "MultipleSmells";
	public static final String DUPLICATE_FUNCTIONALITY = "DuplicateFunctionality";
	public static final String AMBIGUOUS_INTERFACE = "AmbiguousInterface";
	public static final String UNUSED_INCLUDE = "UnusedInclude";
}
