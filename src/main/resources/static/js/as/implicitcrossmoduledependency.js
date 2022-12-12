let implicitCrossModuleDependency = function() {
	let _implicitCrossModuleDependency = function(project, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
		if (project !== null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (fileImplicitCrossModuleDependencyMap !== null) {
				let fileImplicitCrossModuleDependencyList = fileImplicitCrossModuleDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File1</th>";
				html += "<th>File2</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-Change Times</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileImplicitCrossModuleDependencyList) {
					if (fileImplicitCrossModuleDependencyList.hasOwnProperty(fileIndex)) {
						let fileImplicitCrossModuleDependency = fileImplicitCrossModuleDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'><a href='/relation/dependsdetail/" + fileImplicitCrossModuleDependency.node1.id + "_" + fileImplicitCrossModuleDependency.node2.id + "' target='_blank'>" + index + "</td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node1.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node1.path + "</a></td>";
						html += "<td><a href='/relation/file/" + fileImplicitCrossModuleDependency.node2.id + "' target='_blank'>" + fileImplicitCrossModuleDependency.node2.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileImplicitCrossModuleDependency.cochangeTimes + "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}

			if (packageImplicitCrossModuleDependencyMap != null) {
				let packageImplicitCrossModuleDependencyList = packageImplicitCrossModuleDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>Package1</th>";
				html += "<th>Package2</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Co-Change Times</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageImplicitCrossModuleDependencyList) {
					if (packageImplicitCrossModuleDependencyList.hasOwnProperty(packageIndex)) {
						let packageImplicitCrossModuleDependency = packageImplicitCrossModuleDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node1.directoryPath + "</td>";
						html += "<td>" + packageImplicitCrossModuleDependency.node2.directoryPath + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageImplicitCrossModuleDependency.cochangeTimes + "</td>";
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
		implicitCrossModuleDependency: function(project, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap) {
			_implicitCrossModuleDependency(project, fileImplicitCrossModuleDependencyMap, packageImplicitCrossModuleDependencyMap);
		}
	};
};
