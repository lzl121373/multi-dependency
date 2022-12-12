package cn.edu.fudan.se.multidependency.model.node;

import java.util.ArrayList;
import java.util.List;

public enum NodeLabelType {
	Project, Package, ProjectFile,
	Namespace, Type, Function, Variable, Snippet,
	Library, LibraryAPI, License,
	MicroService, RestfulAPI, Span,
	Scenario, TestCase, Feature, Trace, Bug,
	GitRepository, Branch, Commit, Issue, Label, Developer,
	CloneGroup, CodeUnit,
	Module,
	Metric,Smell;
	
	public List<String> labels() {
		List<String> result = new ArrayList<>();
		switch(this) {
			case Metric:
			case Smell:
			case Module:
			case ProjectFile:
			case Namespace:
			case Type:
			case Function:
				result.add(this.toString());
				result.add(CodeUnit.toString());
				break;
			case CodeUnit:
			case Branch:
			case Bug:
			case CloneGroup:
			case Commit:
			case Developer:
			case Feature:
			case GitRepository:
			case Issue:
			case Label:
			case Library:
			case LibraryAPI:
			case License:
			case MicroService:
			case Package:
			case Project:
			case RestfulAPI:
			case Scenario:
			case Snippet:
			case Span:
			case TestCase:
			case Trace:
			case Variable:
				result.add(this.toString());
		}
		
		return result;
	}
	
	public List<String> indexes() {
		List<String> result = new ArrayList<>();
		switch(this) {
			case Branch:
				break;
			case Bug:
				break;
			case CloneGroup:
				result.add("cloneLevel");
				result.add("name");
				break;
			case Commit:
				result.add("commitId");
				break;
			case Developer:
				break;
			case Feature:
				result.add("name");
				break;
			case Function:
				break;
			case GitRepository:
				break;
			case Issue:
				break;
			case Label:
				break;
			case Library:
				break;
			case LibraryAPI:
				break;
			case License:
				break;
			case MicroService:
				result.add("name");
				break;
			case Namespace:
				break;
			case Package:
				result.add("directoryPath");
				break;
			case Project:
				result.add("name");
				result.add("language");
				break;
			case ProjectFile:
				result.add("path");
				break;
			case RestfulAPI:
				break;
			case Scenario:
				break;
			case Snippet:
				break;
			case Span:
				break;
			case TestCase:
				break;
			case Trace:
				result.add("traceId");
				break;
			case Type:
				break;
			case Variable:
				break;
			default:
				break;
		}
		return result;
	}
	
}
