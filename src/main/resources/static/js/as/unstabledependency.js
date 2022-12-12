let unstableDependency = function() {
	let _unstableDependency = function(project, fileUnstableDependencyMap, packageUnstableDependencyMap) {
		if (project !== null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (fileUnstableDependencyMap !== null) {
				let fileUnstableDependencyList = fileUnstableDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Instability</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Outgoing Dependencies</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Outgoing CoChange Files</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commits</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnstableDependencyList) {
					if (fileUnstableDependencyList.hasOwnProperty(fileIndex)) {
						let fileUnstableDependency = fileUnstableDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileUnstableDependency.component.id + "'>" + fileUnstableDependency.component.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableDependency.component.instability).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableDependency.component.score).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableDependency.fanOut + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableDependency.coChangeFiles.length + "</td>";

						let allFilesIds = fileUnstableDependency.component.id;
						for(let j = 0; j < fileUnstableDependency.coChangeFiles.length; j++) {
							allFilesIds += "," + fileUnstableDependency.coChangeFiles[j].id;
						}

						html += "<td style='text-align: center; vertical-align: middle'>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileUnstableDependency.component.id + "&minCount=2'>commits</a>" + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}

			// if (packageUnstableDependencyMap !== null) {
			// 	let packageUnstableDependencyList = packageUnstableDependencyMap[project.id];
			// 	html += "<div>";
			// 	html += "<table class='table table-bordered'>";
			// 	html += "<tr>";
			// 	html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
			// 	html += "<th>Package</th>";
			// 	html += "<th style='text-align: center; vertical-align: middle'>Instability</th>";
			// 	html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
			// 	html += "<th style='text-align: center; vertical-align: middle'>All Outgoing Dependencies</th>";
			// 	html += "<th style='text-align: center; vertical-align: middle'>Bad Outgoing Dependencies</th>";
			// 	html += "</tr>";
			// 	index = 1;
			// 	for(let packageIndex in packageUnstableDependencyList) {
			// 		if (packageUnstableDependencyList.hasOwnProperty(packageIndex)) {
			// 			let packageUnstableDependency = packageUnstableDependencyList[packageIndex];
			// 			html += "<tr>";
			// 			html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
			// 			html += "<td><a target='_blank' href='/relation/file/" + packageUnstableDependency.component.id + "'>" + packageUnstableDependency.component.name + "</a></td>";
			// 			let pck_instability = packageUnstableDependency.component.instability != null ? (packageUnstableDependency.component.instability).toFixed(2) : "NULL"
			// 			html += "<td style='text-align: center; vertical-align: middle'>" + pck_instability  + "</td>";
			// 			let pck_score = packageUnstableDependency.component.score != null ? (packageUnstableDependency.component.score).toFixed(2) : "NULL"
			// 			html += "<td style='text-align: center; vertical-align: middle'>" + pck_score + "</td>";
			// 			html += "<td style='text-align: center; vertical-align: middle'>" + packageUnstableDependency.allDependencies + "</td>";
			// 			html += "<td style='text-align: center; vertical-align: middle'>" + packageUnstableDependency.badDependencies + "</td>";
			// 			html += "</tr>";
			// 			index ++;
			// 		}
			// 	}
			// 	html += "</table>";
			// 	html += "</div>";
			// }
			// html += "</div>";
			$("#content").html(html);
		}
	};
	
	return {
		unstableDependency: function(project, fileUnstableDependencyMap, packageUnstableDependencyMap) {
			_unstableDependency(project, fileUnstableDependencyMap, packageUnstableDependencyMap);
		}
	};
};
