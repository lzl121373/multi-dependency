let smellmetric = function() {
	let allSmellMetric = function(projects) {
    		$.ajax({
    			type: "get",
    			url: "/metric/smellMetric",
    			success: function(result) {
    			    let myObj = {size : result.length };
    				let html = "";
                    let commits;
                    for (let id in projects) {
                        html += "<div><h4>" + projects[id].name + " (" + projects[id].language + ")" + "</h4></div>";
                        // html += "<div><button name='fileTable_" + id + "' class='btn btn-primary file_excel_button'>输出 excel</button></div>";
                        let typeIndex = 0;
                        for (let type in result[id]) {
                            html += "<div><table id='fileTable_" + id + "_" + type + "' class='table table-bordered'>";
                            html += "<div><h5>" + typeIndex++ + ". " + type + "</h5></div>";
                            html += "<tr>";
                            html += "<th>Index</th>";
                            html += "<th>Project</th>";
                            html += "<th>Smell</th>";
                            html += "<th>Size</th>";
                            html += "<th>LOC</th>";
                            html += "<th>NOP</th>";
                            html += "<th>NOF</th>";
                            html += "<th>NOC</th>";
                            html += "<th>NOM</th>";
                            html += "<th>Developers</th>";
                            html += "<th>Commits</th>";
                            html += "<th>CoCommits</th>";
                            html += "<th>CoFiles</th>";
                            html += "<th>Avg(Developers)</th>";
                            html += "<th>Avg(#D)</th>";
                            html += "<th>Avg(Commits)</th>";
                            html += "<th>Avg(#C)</th>";
                            html += "<th>Avg(CoCommits)</th>";
                            html += "<th>Avg(#CoC)</th>";
                            html += "<th>AddLines</th>";
                            html += "<th>Avg(#AddLines)</th>";
                            html += "<th>SubLines</th>";
                            html += "<th>Avg(#SubLines)</th>";
                            html += "<th>Issues</th>";
                            html += "<th>Bugs</th>";
                            html += "<th>NewFeatures</th>";
                            html += "<th>Improvements</th>";
                            html += "</tr>";
                            let metrics = result[id][type];
                            for (let i = 0; i < metrics.length; i++) {
                                html += "<tr>";
                                html += "<td>" + (i + 1) + "</td>";
                                html += "<td>" + metrics[i].node.projectName + "</td>";
                                html += "<td>" + metrics[i].node.name + "</td>";
                                html += "<td>" + metrics[i].node.size + "</td>";

                                let nop = metrics[i].metric.metricValues.NOP;
                                let nof = metrics[i].metric.metricValues.NOF;
                                let loc = metrics[i].metric.metricValues.LOC;
                                let noc = metrics[i].metric.metricValues.NOC;
                                let nom = metrics[i].metric.metricValues.NOM;
                                html += "<td>" + loc + "</td>";
                                html += "<td>" + nop + "</td>";
                                html += "<td>" + nof + "</td>";
                                html += "<td>" + noc + "</td>";
                                html += "<td>" + nom + "</td>";

                                let commits = metrics[i].metric.metricValues.Commits;
                                let totalCommits = metrics[i].metric.metricValues.TotalCommits;
                                let developers = metrics[i].metric.metricValues.Developers;
                                let totalDevelopers = metrics[i].metric.metricValues.TotalDevelopers;
                                let addLines = metrics[i].metric.metricValues.AddLines;
                                let subLines = metrics[i].metric.metricValues.SubLines;

                                let coChangeCommits = metrics[i].metric.metricValues.CoChangeCommits;
                                let totalCoChangeCommits = metrics[i].metric.metricValues.TotalCoChangeCommits;
                                let coChangeFiles = metrics[i].metric.metricValues.CoChangeFiles;

                                let size = metrics[i].node.size
                                html += "<td>" + developers + "</td>";
                                html += "<td>" + commits + "</td>";
                                html += "<td>" + coChangeCommits + "</td>";
                                html += "<td>" + coChangeFiles + "</td>";
                                html += "<td>" + developers + "/" + size + "=" + (developers / size).toFixed(2) + "</td>";
                                html += "<td>" + (developers / size).toFixed(2) + "</td>";
                                html += "<td>" + totalCommits + "/" + size + "=" + (totalCommits / size).toFixed(2) + "</td>";
                                html += "<td>" + (totalCommits / size).toFixed(2) + "</td>";
                                html += "<td>" + totalCoChangeCommits + "/" + totalCommits + "="
                                    + (totalCommits > 0 ? (totalCoChangeCommits / totalCommits).toFixed(2) : 0) + "</td>";
                                html += "<td>" + (totalCommits > 0 ? (totalCoChangeCommits / totalCommits).toFixed(2) : 0) + "</td>";

                                html += "<td>" + addLines + "</td>";
                                html += "<td>" + (addLines / size).toFixed(2) + "</td>";
                                html += "<td>" + subLines + "</td>";
                                html += "<td>" + (subLines / size).toFixed(2) + "</td>";

                                let issues = metrics[i].metric.metricValues.Issues;
                                let bugIssues = metrics[i].metric.metricValues.BugIssues;
                                let newFeatureIssues = metrics[i].metric.metricValues.NewFeatureIssues;
                                let improvementIssues = metrics[i].metric.metricValues.ImprovementIssues;

                                html += "<td>" + issues + "</td>";
                                html += "<td>" + bugIssues + "</td>";
                                html += "<td>" + newFeatureIssues + "</td>";
                                html += "<td>" + improvementIssues + "</td>";
                                //    						html += "<td>" + metrics[i].file.score.toFixed(2) + "</td>";
                                html += "</tr>";
                            }
                            html += "</table></div>";
                        }
                    }
    				$("#smellMetric").html(html);
//    				$(".smell_excel_button").click(function() {
//    					tableToExcel($(this).attr("name"), "allSmellMetric");
//    				});
    			}
    		})
    	}
	return {
		init: function() {
			$.ajax({
				type: "get",
				url: "/project/all",
				success: function(result) {
                    let info = "<p>获取中...</p>"
                    $("#smellMetric").html(info);
					allSmellMetric(result);
				}
			});
		}
	}
}