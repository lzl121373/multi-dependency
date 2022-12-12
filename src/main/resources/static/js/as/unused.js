let unused = function(cytoscapeutil) {
	let _unused = function(projects, packages) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<h4>" + project.name + " (" + project.language + ")</h4>";

				let unusedPackages = packages[project.id];
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th>Package</th>";
				html += "</tr>";
				for(let packageIndex in unusedPackages) {
					if (unusedPackages.hasOwnProperty(packageIndex)) {
						let pck = unusedPackages[packageIndex];
						html += "<tr>";
						html += "<td width='50%'>" + pck.directoryPath + "</td>";
						html += "</tr>";
					}
				}
				html += "</table>";
			}
		}
		$("#content").html(html);
	}
	
	return {
		unused: function(projects, packages) {
			_unused(projects, packages);
		}
	}
}
