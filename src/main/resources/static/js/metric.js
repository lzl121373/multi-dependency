var metric = function() {
	var fileMetric = function(projects) {
		$.ajax({
			type: "get",
			url: "/metric/file",
			success: function(result) {
				// console.log(result);
				var html = "";
				for(var id in projects) {
					html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
					// html += "<div><button name='fileTable_" + id + "' class='btn btn-primary file_excel_button'>输出 excel</button></div>";
					html += "<div><table id='fileTable_" + id + "' class='table table-bordered'>";
					html += "<tr>";
					html += "<th>Index</th>";
					html += "<th>Id</th>";
					html += "<th>ProjectFile</th>";
					html += "<th>LOC</th>";
					html += "<th>NOC</th>";
					html += "<th>NOM</th>";
					html += "<th>FanIn</th>";
					html += "<th>FanOut</th>";
					html += "<th>Instability</th>";
					html += "<th>Commits</th>";
					html += "<th>Developers</th>";
//					html += "<th>CoChanges</th>";
					html += "<th>CoFiles</th>";
					html += "<th>Issues</th>";
					html += "<th>Bugs</th>";
					html += "<th>NewFeatures</th>";
					html += "<th>Improvements</th>";
					html += "<th>PageRank</th>";
					html += "</tr>";
					var metrics = result[id];
					// console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						// console.log(metrics[i]);
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].node.id + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + metrics[i].node.id + "'>" + metrics[i].node.path + "</a></td>";

						let loc = metrics[i].metric.metricValues.LOC;
						let noc = metrics[i].metric.metricValues.NOC;
						let nom = metrics[i].metric.metricValues.NOM;
						let fanIn = metrics[i].metric.metricValues.FanIn;
						let fanOut = metrics[i].metric.metricValues.FanOut;
						let instability = metrics[i].metric.metricValues.Instability;
						html += "<td>" + (loc > 0 ? loc : 0)  + "</td>";
						html += "<td>" + (noc > 0 ? noc : 0)  + "</td>";
						html += "<td>" + (nom > 0 ? nom : 0)  + "</td>";
						html += "<td>" + (fanIn > 0 ? fanIn : 0)  + "</td>";
						html += "<td>" + (fanOut > 0 ? fanOut : 0)  + "</td>";
						html += "<td>" + (instability > 0 ? instability.toFixed(2) : 0.0)  + "</td>";

						let  commits = metrics[i].metric.metricValues.Commits;
						let  developers = metrics[i].metric.metricValues.Developers;
//                         let    coChanges = metrics[i].metric.metricValues.coChanges;
						let coChangeFiles = metrics[i].metric.metricValues.CoChangeFiles;
						html += "<td>" + (commits > 0 ? commits : 0)  + "</td>";
						html += "<td>" + (developers > 0 ? developers : 0)  + "</td>";
						// html += "<td>" + (coChanges > 0 ? coChanges : 0)  + "</td>";
						html += "<td>" + (coChangeFiles > 0 ? coChangeFiles : 0)  + "</td>";

						let issues = metrics[i].metric.metricValues.Issues;
						let bugIssues = metrics[i].metric.metricValues.BugIssues;
						let newFeatureIssues = metrics[i].metric.metricValues.NewFeatureIssues;
						let improvementIssues = metrics[i].metric.metricValues.ImprovementIssues;
						let pageRankScore = metrics[i].metric.metricValues.PageRankScore;
                        html += "<td>" + (issues > 0 ? issues : 0) + "</td>";
						html += "<td>" + (bugIssues > 0 ? bugIssues : 0) + "</td>";
						html += "<td>" + (newFeatureIssues > 0 ? newFeatureIssues : 0) + "</td>";
						html += "<td>" + (improvementIssues > 0 ? improvementIssues : 0) + "</td>";
						html += "<td>" + (pageRankScore > 0 ? pageRankScore.toFixed(2) : 0.0) + "</td>";
						html += "</tr>";
					}
					html += "</table></div>";
				}
				$("#fileMetrics").html(html);
				// $(".file_excel_button").click(function() {
				// 	tableToExcel($(this).attr("name"), "fileMetrics");
				// });
			}
		})
	}

	var packageMetric = function(projects) {
		$.ajax({
			type: "get",
			url: "/metric/package",
			success: function(result) {
				// console.log(result);
				var html = "";
				for(var id in projects) {
					html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
					// html += "<div><button name='packageTable_" + id + "' class='btn btn-primary package_excel_button'>输出 excel</button></div>";
					html += "<div><table id='packageTable_" + i + "' class='table table-bordered'>";
					html += "<tr>";
					html += "<th>Index</th>";
					html += "<th>Id</th>";
					html += "<th>Package/Directory</th>";
					html += "<th>NOF</th>";
					html += "<th>NOC</th>";
					html += "<th>NOM</th>";
					html += "<th>LOC</th>";
					html += "<th>Lines</th>";
					html += "<th>Ca</th>";
					html += "<th>Ce</th>";
					html += "<th>Instability</th>";
					html += "</tr>";
					var metrics = result[id];
					// console.log(metrics);
					for(var i = 0; i < metrics.length; i++) {
						html += "<tr>";
						html += "<td>" + (i + 1) + "</td>";
						html += "<td>" + metrics[i].node.id + "</td>";
						html += "<td><a target='_blank' href='/relation/package/" + metrics[i].node.id + "'>" + metrics[i].node.directoryPath + "</a></td>";
						let nof = metrics[i].metric.metricValues.NOF;
						let noc = metrics[i].metric.metricValues.NOC;
						let nom = metrics[i].metric.metricValues.NOM;
						let loc = metrics[i].metric.metricValues.LOC;
						let lines = metrics[i].metric.metricValues.Lines;
						let fanIn = metrics[i].metric.metricValues.FanIn;
						let fanOut = metrics[i].metric.metricValues.FanOut;
						let instability = metrics[i].metric.metricValues.Instability;
						html += "<td>" + (nof > 0 ? nof : 0) + "</td>";
						html += "<td>" + (noc > 0 ? noc : 0) + "</td>";
						html += "<td>" + (nom > 0 ? nom : 0) + "</td>";
						html += "<td>" + (loc > 0 ? loc : 0) + "</td>";
						html += "<td>" + (lines > 0 ? lines : 0) + "</td>";
						html += "<td>" + (fanIn > 0 ? fanIn : 0) + "</td>";
						html += "<td>" + (fanOut > 0 ? fanOut : 0) + "</td>";
						html += "<td>" + (instability > 0 ? (instability).toFixed(2) : 0.0) + "</td>";
						html += "</tr>";
					}
					html += "</table></div>";
				}
				$("#packageMetrics").html(html);
				// $(".package_excel_button").click(function() {
				// 	tableToExcel($(this).attr("name"), "packageMetrics");
				// });
			}
		});
	}
	
	var showModularity = function(container, projectId) {
		$.ajax({
			type: "get",
			url: "/metric/project/modularity?projectId=" + projectId,
			success: function(modularity) {
				container.text(modularity.toFixed(2));
			}
		});
	}
	
	var showCommitTimes = function(container, projectId) {
		$.ajax({
			type: "get",
			url: "/metric/project/commitTimes?projectId=" + projectId,
			success: function(commitTimes) {
				container.text(commitTimes);
			}
		});
	}
	
	var projectMetric = function() {
		$.ajax({
			type: "get",
			url: "/metric/project",
			success: function(result) {
				// console.log(result);
				var html = "<table id='projectTable' class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>Id</th>";
				html += "<th>Project</th>";
				html += "<th>NOP</th>";
				html += "<th>NOF</th>";
				html += "<th>NOC</th>";
				html += "<th>NOM</th>";
				html += "<th>LOC</th>";
				html += "<th>Lines</th>";
				html += "<th>Commits</th>";
				html += "<th>Developers</th>";
				html += "<th>Issues</th>";
				html += "<th>Modularity</th>";
				html += "</tr>";
				for(var i = 0; i < result.length; i++) {
					html += "<tr>";
					html += "<td>" + (i + 1) + "</td>";
					html += "<td>" + result[i].node.id + "</td>";
					html += "<td>" + result[i].node.name + " (" + result[i].node.language + ") " + "</td>";
					let nop = result[i].metric.metricValues.NOP;
					let nof = result[i].metric.metricValues.NOF;
					let noc = result[i].metric.metricValues.NOC;
					let nom = result[i].metric.metricValues.NOM;
					let loc = result[i].metric.metricValues.LOC;
					let lines = result[i].metric.metricValues.Lines;
					let commits = result[i].metric.metricValues.Commits;
					let developers = result[i].metric.metricValues.Developers;
					let issues = result[i].metric.metricValues.Issues;
					let modularity = result[i].metric.metricValues.MODULARITY;
					html += "<td>" + (nop > 0 ? nop : 0) + "</td>";
					html += "<td>" + (nof > 0 ? nof : 0) + "</td>";
					html += "<td>" + (noc > 0 ? noc : 0) + "</td>";
					html += "<td>" + (nom > 0 ? nom : 0)+ "</td>";
					html += "<td>" + (loc > 0 ? loc : 0) + "</td>";
					html += "<td>" + (lines > 0 ? lines : 0) + "</td>";
					html += "<td>" + (commits > 0 ? commits : 0) + "</td>";
					html += "<td>" + (developers > 0 ? developers : 0) + "</td>";
					html += "<td>" + (issues > 0 ? issues : 0) + "</td>";
					html += "<td id='modularity_" + result[i].node.id + "'>" + (modularity > 0 ? modularity.toFixed(2) : "计算中...") + "</td>";
					html += "</tr>";
				}
				html += "</table>";
				$("#projectMetrics").html(html);
				for(var i = 0; i < result.length; i++) {
					var projectId = result[i].node.id;
					showModularity($("#modularity_" + projectId), projectId);
					// showCommitTimes($("#commitTimes_" + projectId), projectId);
				}
			}
		});
	}
	return {
		init: function() {
			var info = "<p>获取中...</p>"
			$("#projectMetrics").html(info);
			projectMetric();
			$.ajax({
				type: "get",
				url: "/project/all",
				success: function(result) {
					console.log(result);
					var info = "<p>获取中...</p>"
					$("#packageMetrics").html(info);
					$("#fileMetrics").html(info);
					packageMetric(result);
					fileMetric(result);
				}
			});
			// $("#projectButton").click(function() {
			// 	tableToExcel("projectTable", "projectMetrics");
			// });
// 			$("#fileButton").click(function() {
// //				window.href="/";
// 				$.ajax({
// 					type: "get",
// 					url: "/metric/excel/file"
// 				});
// 			});
		}
	}
}