let unusedInclude = function() {
	const SMELL_TYPE = {
		UNUSED_INCLUDE: "UnusedInclude"
	};
	const SMELL_LEVEL = {
		FILE: "File"
	};
	let _unusedInclude = function(project, fileUnusedIncludeMap) {
		if (project !== null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (fileUnusedIncludeMap !== null){
				let fileUnusedIncludeList = fileUnusedIncludeMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th style='vertical-align: middle'>CoreFile</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Number</th>";
				html += "<th style='vertical-align: middle'>UnusedIncludeFiles</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnusedIncludeList) {
					if (fileUnusedIncludeList.hasOwnProperty(fileIndex)) {
						let fileUnusedInclude = fileUnusedIncludeList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='/as/smellgraph" + "?projectid=" + project.id + "&smelltype=" + SMELL_TYPE.UNUSED_INCLUDE + "&smelllevel=" + SMELL_LEVEL.FILE + "&smellindex=" + index + "'>" + index + "</a>" +
							"</td>";
						html += "<td style='vertical-align: middle'>" + fileUnusedInclude.coreFile.path + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnusedInclude.unusedIncludeFiles.length + "</td>";
						html += "<td style='vertical-align: middle'>";
						for(let i = 0; i < fileUnusedInclude.unusedIncludeFiles.length; i++) {
							html += fileUnusedInclude.unusedIncludeFiles[i].path + "<br/>";
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
		unusedInclude: function(project, fileUnusedIncludeMap) {
			_unusedInclude(project, fileUnusedIncludeMap);
		}
	};
};
