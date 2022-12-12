let similarComponents = function() {
	let _similarComponents = function(projects, fileSimilarComponentsMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<div>";
				html += "<div>";
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";
				html += "</div>";

				let fileSimilarComponentsList = fileSimilarComponentsMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Index</th>";
				html += "<th>File</th>";
				html += "<th>Module</th>";
				html += "<th>Clone Type</th>";
				html += "<th>Clone Value</th>";
				html += "<th>Change Times</th>";
				html += "<th>Co-Change Times</th>";
				html += "<th>Ratio of Depends-On</th>";
				html += "<th>Commits</th>";
				html += "</tr>";
				let index = 1;
				for (let fileIndex in fileSimilarComponentsList) {
					if (fileSimilarComponentsList.hasOwnProperty(fileIndex)) {
						let fileSimilarComponents = fileSimilarComponentsList[fileIndex];
						html += "<tr>";
						html += "<td rowspan='2' style='vertical-align: middle'>" + index + "</td>";
						html += "<td style='vertical-align: middle'><a target='_blank' href='/relation/file/" + fileSimilarComponents.node1.id + "'>" + fileSimilarComponents.node1.path + "</a></td>";
						html += "<td style='vertical-align: middle'>" + "module_" + fileSimilarComponents.module1.id + "</td>";
						html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.cloneType + "</td>";
						html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.value.toFixed(2) + "</td>";
						html += "<td style='vertical-align: middle'>" + fileSimilarComponents.node1ChangeTimes + "</td>";
						html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.cochangeTimes + "</td>";
						html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.sameDependsOnRatio.toFixed(2) + "</td>";
						html += "<td rowspan='2' style='vertical-align: middle'><a target='_blank' href='/as/matrix?allFiles=" + fileSimilarComponents.node1.id + "," + fileSimilarComponents.node2.id + "&specifiedFiles=" + fileSimilarComponents.node1.id + "," + fileSimilarComponents.node2.id + "&minCount=2" + "'" + fileIndex + "'>commits</a></td>";
						html += "</tr>";
						html += "<tr>";
						html += "<td style='vertical-align: middle'><a target='_blank' href='/relation/file/" + fileSimilarComponents.node2.id + "'>" + fileSimilarComponents.node2.path + "</a></td>";
						html += "<td style='vertical-align: middle'>" + "module_" + fileSimilarComponents.module2.id + "</td>";
						html += "<td style='vertical-align: middle'>" + fileSimilarComponents.node2ChangeTimes + "</td>";
						html += "</tr>";
						index++;
					}
				}
				html += "</table>";
				html += "</div>";
				html += "</div>";
			}
		}
		if (fileSimilarComponentsMap[-1] != null) {
			html += "<h4>Other</h4>";
			let fileSimilarComponentsList = fileSimilarComponentsMap[-1];

			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Index</th>";
			html += "<th>File</th>";
			html += "<th>Module</th>";
			html += "<th>Clone Type</th>";
			html += "<th>Clone Value</th>";
			html += "<th>Change Times</th>";
			html += "<th>Co-Change Times</th>";
			html += "<th>Ratio of Depends-On</th>";
			html += "<th>Commits</th>";
			html += "</tr>";
			let index = 1;
			for (let fileIndex in fileSimilarComponentsList) {
				if (fileSimilarComponentsList.hasOwnProperty(fileIndex)) {
					let fileSimilarComponents = fileSimilarComponentsList[fileIndex];
					html += "<tr>";
					html += "<td rowspan='2' style='vertical-align: middle'>" + index + "</td>";
					html += "<td style='vertical-align: middle'><a target='_blank' href='/relation/file/" + fileSimilarComponents.node1.id + "'>" + fileSimilarComponents.node1.path + "</a></td>";
					html += "<td style='vertical-align: middle'>" + "module_" + fileSimilarComponents.module1.id + "</td>";
					html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.cloneType + "</td>";
					html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.value.toFixed(2) + "</td>";
					html += "<td style='vertical-align: middle'>" + fileSimilarComponents.node1ChangeTimes + "</td>";
					html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.cochangeTimes + "</td>";
					html += "<td rowspan='2' style='vertical-align: middle'>" + fileSimilarComponents.sameDependsOnRatio.toFixed(2) + "</td>";
					html += "<td rowspan='2' style='vertical-align: middle'><a target='_blank' href='/as/matrix?allFiles=" + fileSimilarComponents.node1.id + "," + fileSimilarComponents.node2.id + "&specifiedFiles=" + fileSimilarComponents.node1.id + "," + fileSimilarComponents.node2.id + "&minCount=2" + "'" + fileIndex + "'>commits</a></td>";
					html += "</tr>";
					html += "<tr>";
					html += "<td style='vertical-align: middle'><a target='_blank' href='/relation/file/" + fileSimilarComponents.node2.id + "'>" + fileSimilarComponents.node2.path + "</a></td>";
					html += "<td style='vertical-align: middle'>" + "module_" + fileSimilarComponents.module2.id + "</td>";
					html += "<td style='vertical-align: middle'>" + fileSimilarComponents.node2ChangeTimes + "</td>";
					html += "</tr>";
					index++;
				}
			}
			html += "</table>";
		}
		$("#content").html(html);
	}
	
	return {
		similarComponents : function(projects, fileSimilarComponentsMap) {
			_similarComponents(projects, fileSimilarComponentsMap);
		}
	}
}
