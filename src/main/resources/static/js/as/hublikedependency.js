let hubLikeDependency = function() {
	let _hubLikeDependency = function(project, fileHubLikeDependencyMap, packageHubLikeDependencyMap) {
		if (project !== null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (fileHubLikeDependencyMap !== null) {
				let fileHubLikeDependencyList = fileHubLikeDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>FanIn</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesIn/FanIn</th>";
				html += "<th style='text-align: center; vertical-align: middle'>FanOut</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesOut/FanOut</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeFilesAll/(FanIn+FinOut)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-ChangeCommits</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "</tr>";
				let index = 1;
				for (let fileIndex in fileHubLikeDependencyList) {
					if (fileHubLikeDependencyList.hasOwnProperty(fileIndex)) {
						let fileHubLikeDependency = fileHubLikeDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileHubLikeDependency.file.id + "'>" + fileHubLikeDependency.file.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.fanIn + "</td>";
						let inRatio = (fileHubLikeDependency.coChangeFilesIn.length / fileHubLikeDependency.fanIn).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.coChangeFilesIn.length + "/" + fileHubLikeDependency.fanIn + "=" + inRatio + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.fanOut + "</td>";
						let outRatio = (fileHubLikeDependency.coChangeFilesOut.length / fileHubLikeDependency.fanOut).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>" + fileHubLikeDependency.coChangeFilesOut.length + "/" + fileHubLikeDependency.fanOut + "=" + outRatio + "</td>";
						let allIORatio = ((fileHubLikeDependency.coChangeFilesIn.length + fileHubLikeDependency.coChangeFilesOut.length) / (fileHubLikeDependency.fanIn + fileHubLikeDependency.fanOut)).toFixed(2);
						html += "<td style='text-align: center; vertical-align: middle'>(" + fileHubLikeDependency.coChangeFilesIn.length + "+" + fileHubLikeDependency.coChangeFilesOut.length + ")/(" ;
						html += fileHubLikeDependency.fanIn + "+" + fileHubLikeDependency.fanOut + ")=" + allIORatio + "</td>";
						let allFilesIds = fileHubLikeDependency.file.id;
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesIn.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesIn[j].id;
						}
						for (let j = 0; j < fileHubLikeDependency.coChangeFilesOut.length; j++) {
							allFilesIds += "," + fileHubLikeDependency.coChangeFilesOut[j].id;
						}
						html += "<td style='text-align: center; vertical-align: middle'>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileHubLikeDependency.file.id + "&minCount=2'>commits</a>" + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileHubLikeDependency.file.score).toFixed(2) + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}

			if (packageHubLikeDependencyMap !== null) {
				let packageHubLikeDependencyList = packageHubLikeDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>Package</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Ca（afferent couplings）</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Ce（efferent couplings）</th>";
				html += "</tr>";
				index = 1;
				for (let packageIndex in packageHubLikeDependencyList) {
					if (packageHubLikeDependencyList.hasOwnProperty(packageIndex)) {
						let packageHubLikeDependency = packageHubLikeDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td>" + packageHubLikeDependency.pck.name + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageHubLikeDependency.fanIn + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageHubLikeDependency.fanOut + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}
			html += "</div>";
			$("#content").html(html);
		}
	};
	
	return {
		hubLikeDependency: function(project, fileHubLikeDependencyMap, packageHubLikeDependencyMap) {
			_hubLikeDependency(project, fileHubLikeDependencyMap, packageHubLikeDependencyMap);
		}
	};
};
