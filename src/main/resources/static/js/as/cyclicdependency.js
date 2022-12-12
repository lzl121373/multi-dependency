let cyclicDependency = function() {
	const SMELL_TYPE = {
		CYCLIC_DEPENDENCY: "CyclicDependency"
	};
	const SMELL_LEVEL = {
		TYPE: "Type",
		FILE: "File",
		PACKAGE: "Package"
	};
	let _cyclicDependency = function(project, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap) {
		if (project != null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (typeCyclicDependencyMap != null) {
				let typeCyclicDependencyList = typeCyclicDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Types</th>";
				html += "</tr>";
				let index = 1;
				for(let typeIndex in typeCyclicDependencyList) {
					if (typeCyclicDependencyList.hasOwnProperty(typeIndex)) {
						let typeCyclicDependency = typeCyclicDependencyList[typeIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='/as/smellgraph" + "?projectid=" + project.id + "&smelltype=" + SMELL_TYPE.CYCLIC_DEPENDENCY + "&smelllevel=" + SMELL_LEVEL.TYPE + "&smellindex=" + index + "'>" + index + "</a>" +
							"</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + typeCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < typeCyclicDependency.components.length; i++) {
							html += typeCyclicDependency.components[i].name + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}

			if (fileCyclicDependencyMap != null) {
				let fileCyclicDependencyList = fileCyclicDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Files</th>";
				html += "</tr>";
				index = 1;
				for(let fileIndex in fileCyclicDependencyList) {
					if (fileCyclicDependencyList.hasOwnProperty(fileIndex)) {
						let fileCyclicDependency = fileCyclicDependencyList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='/as/smellgraph" + "?projectid=" + project.id + "&smelltype=" + SMELL_TYPE.CYCLIC_DEPENDENCY + "&smelllevel=" + SMELL_LEVEL.FILE + "&smellindex=" + index + "'>" + index + "</a>" +
							"</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < fileCyclicDependency.components.length; i++) {
							html += fileCyclicDependency.components[i].path + "<br/>";
						}
						html += "</td>";
						html += "</tr>";
						index ++;
					}
				}
				html += "</table>";
				html += "</div>";
			}

			if (packageCyclicDependencyMap != null) {
				let packageCyclicDependencyList = packageCyclicDependencyMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th>Packages</th>";
				html += "</tr>";
				index = 1;
				for(let packageIndex in packageCyclicDependencyList) {
					if (packageCyclicDependencyList.hasOwnProperty(packageIndex)) {
						let packageCyclicDependency = packageCyclicDependencyList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='/as/smellgraph" + "?projectid=" + project.id + "&smelltype=" + SMELL_TYPE.CYCLIC_DEPENDENCY + "&smelllevel=" + SMELL_LEVEL.PACKAGE + "&smellindex=" + index + "'>" + index + "</a>" +
							"</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + packageCyclicDependency.components.length + "</td>";
						html += "<td>";
						for(let i = 0; i < packageCyclicDependency.components.length; i++) {
							html += packageCyclicDependency.components[i].name + "<br/>";
						}
						html += "</td>";
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
		cyclicDependency: function(project, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap) {
			_cyclicDependency(project, typeCyclicDependencyMap, fileCyclicDependencyMap, packageCyclicDependencyMap);
		}
	};
};
