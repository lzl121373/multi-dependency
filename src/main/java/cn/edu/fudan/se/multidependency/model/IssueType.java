package cn.edu.fudan.se.multidependency.model;

public final class IssueType {
	public static String typeOfIssue(String type) {
		switch(type) {
			case "Bug":
				return BUG;
			case "Improvement":
				return IMPROVEMENT;
			case "New Feature":
				return NEW_FEATURE;
			case "Task":
				return TASK;
			case "Custom Issue":
				return CUSTOM_ISSUE;
		}
		return DEFAULT_ISSUE;
	}

	/**
	 * 代码总规模， 包括空行和注释
	 */
	public static final String BUG = "Bug";
	public static final String IMPROVEMENT = "Improvement";
	public static final String NEW_FEATURE = "NewFeature";
	public static final String TASK = "Task";
	public static final String CUSTOM_ISSUE = "CustomIssue";
	public static final String DEFAULT_ISSUE = "DefaultIssue";

}
