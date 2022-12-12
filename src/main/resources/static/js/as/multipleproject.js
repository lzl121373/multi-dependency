var multipleproject = function(project, circlePacking, cytoscapeutil) {
	console.log(project);
	console.log(circlePacking);
	var _multiple = function() {
		var html = "";
		var data = {
				"children": [{
					"children": [{
							"size": 5,
							"name": "TraverserRewrite.java"
						},{
							"size": 7,
							"name": "CharMatcherRewrite.java"
						}],
						"name": "default"
				},{
					"children": [{
							"size": 3,
							"name": "XmlEscapersTest.java"
					}],
					"name": "com.google.common.xml"
				}],
				"name": "google__fdse__guava"
		}
		data = {};
		data.children = [];
		var maxIssueSize = 0;
		for(var i = 0; i < circlePacking.length; i++) {
			maxIssueSize = maxIssueSize > circlePacking[i].maxIssueSize ? maxIssueSize : circlePacking[i].maxIssueSize;
		}
		for(var i = 0; i < circlePacking.length; i++) {
			var children = [];
			for(var j = 0; j < circlePacking[i].files.length; j++) {
				console.log(circlePacking[i]);
				children[j] = {
					"name" : circlePacking[i].files[j].id 
						+ "(I: " + circlePacking[i].fileIdToIssues[circlePacking[i].files[j].id].length + ")"
						+ "(S: " + circlePacking[i].fileIdToSmellCount[circlePacking[i].files[j].id] + ")"
						+ "(C: " + circlePacking[i].fileIdToCommitsCount[circlePacking[i].files[j].id] + ")"
						,
					"size" : (circlePacking[i].fileIdToIssues[circlePacking[i].files[j].id].length + 1) * 100,
					"count" : (circlePacking[i].fileIdToSmellCount[circlePacking[i].files[j].id]),
					"id" : circlePacking[i].files[j].id
				}
			}
			data.children[i] = {
				"name" : circlePacking[i].type,
				"children" : children
			}
		}
		projectToGraph(data, "graph");
	}
	
	return {
		init : function() {
			_multiple();
		}
	}
}
