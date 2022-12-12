var rFile = function(fileId, cytoscapeutil) {
	var _file = function() {
		metric(fileId);
		containNamespace(fileId);
		containType(fileId);
		containVariable(fileId);
		containFunction(fileId);
		// depends(fileId);
		issues(fileId);
		commits(fileId);
	};
	
	var commits = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/commit/matrix",
			success: function(result) {
				console.log("success");
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<td></td>";
				for(var k = 0; k < result.files.length; k++) {
					html += "<td>" + k + ":" + "<a target='_blank' href='/relation/file/" + result.files[k].id + "'>";
					html += result.files[k].name;
					html += "</a>(" + result.commitTimes[result.files[k].id] + ")</td>";
				}
				html += "</tr>";
				for(var i = 0; i < result.commits.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + ":" + "<a target='_blank' href='/commit/" + result.commits[i].id + "'>" + result.commits[i].commitId + "(" + result.commits[i].commitFilesSize + ") </a></td>";
					for(var j = 0; j < result.files.length; j++) {
						if(result.update[i][j] === true) {
							html += "<td>T</td>";
						} else {
							html += "<td></td>";
						}
					}
					html += "</tr>";
				}
				html += "</table>";
				$("#commit_content").html(html);
			}
		})
	}
	
	var issues = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/issue",
			success: function(result) {
				// console.log(result);
				var html = "<ol>";
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/issue/" + result[i].id + "'>" + result[i].issueKey + "(" + result[i].type + "): " + result[i].title + "</a></li>";
				}
				html += "</ol>";
				$("#issue_content").html(html);
			}
		})
	}
	
	var metric = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/metric",
			success: function(result) {
				// console.log(result);
				var html = "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<td>LOC</td>";
				html += "<td>NOC</td>";
				html += "<td>NOM</td>";
				html += "<td>FanIn</td>";
				html += "<td>FanOut</td>";
				html += "<td>Instability</td>";
				html += "<td>Commits</td>";
				html += "<td>Developers</td>";
				html += "<td>CoFiles</td>";
				html += "<td>Bugs</td>";
				html += "<td>NewFeatures</td>";
				html += "<td>Improvements</td>";
				html += "<td>PageRank</td>";
				html += "</tr>";
				html += "<tr>";
				let loc = result.metricValues.LOC;
				let noc = result.metricValues.NOC;
				let nom = result.metricValues.NOM;
				let fanIn = result.metricValues.FanIn;
				let fanOut = result.metricValues.FanOut;
				let instability = result.metricValues.Instability;
				let commits = result.metricValues.Commits;
				let developers = result.metricValues.Developers;
				let coChangeFiles = result.metricValues.CoChangeFiles;
				let bugIssues = result.metricValues.BugIssues;
				let newFeatureIssues = result.metricValues.NewFeatureIssues;
				let improvementIssues = result.metricValues.ImprovementIssues;
				let score = result.metricValues.PageRankScore;
				html += "<td>" + (loc > 0 ? loc : 0)  + "</td>";
				html += "<td>" + (noc > 0 ? noc : 0)  + "</td>";
				html += "<td>" + (nom > 0 ? nom : 0)  + "</td>";
				html += "<td>" + (fanIn > 0 ? fanIn : 0)  + "</td>";
				html += "<td>" + (fanOut > 0 ? fanOut : 0)  + "</td>";
				html += "<td>" + (instability > 0 ? instability.toFixed(2) : 0.0)  + "</td>";
				html += "<td>" + (commits > 0 ? commits : 0)  + "</td>";
				html += "<td>" + (developers > 0 ? developers : 0)  + "</td>";
				html += "<td>" + (coChangeFiles > 0 ? coChangeFiles : 0)  + "</td>";
				html += "<td>" + (bugIssues > 0 ? bugIssues : 0)  + "</td>";
				html += "<td>" + (newFeatureIssues > 0 ? newFeatureIssues : 0)  + "</td>";
				html += "<td>" + (improvementIssues > 0 ? improvementIssues : 0)  + "</td>";
				html += "<td>" + (score > 0 ? score.toFixed(2) : 0.0) + "</td>";
				html += "</tr>";
				html += "</table>";
				$("#metric_content").html(html);
			}
		})
	}

	var containNamespace = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/contain/namespace",
			success: function(result) {
				var html = "<ul>";
				// console.log(result);
				for(var i = 0; i < result.length; i++) {
					// html += "<li><a target='_blank' href='/relation/namespace/" + result[i].id + "' >";
					html += "<li><a>";
					html += (result[i].name != "" ? result[i].name : result[i].identifier);
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_namespace_content").html(html);
			}
		});
	}
	
	var containType = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/contain/type",
			success: function(result) {
				var html = "<ul>";
				// console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += (result[i].name != "" ? result[i].name : result[i].identifier);
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_type_content").html(html);
			}
		});
	}

	var containVariable = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/contain/variable",
			success: function(result) {
				var html = "<ul>";
				// console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/variable/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_variable_content").html(html);
			}
		});
	}

	var containFunction = function(fileId) {
		$.ajax({
			type: "get",
			url: "/relation/file/" + fileId + "/contain/function",
			success: function(result) {
				var html = "<ul>";
				// console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/function/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ul>";
				$("#contain_function_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_file();
		}
	}
}