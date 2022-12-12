package cn.edu.fudan.se.multidependency.model.relation.clone;

public enum CloneType {
	type_1, type_2, type_3;
	
	public static CloneType getCloneType(String type) {
		switch(type) {
			case "1":
				return type_1;
			case "2":
				return type_2;
			case "3":
				return type_3;
			default:
				return null;
		}
	}
}
