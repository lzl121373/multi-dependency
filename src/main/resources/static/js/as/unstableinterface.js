let unstableInterface = function() {
	let _unstableInterface = function(project, fileUnstableInterfaceMap, packageUnstableInterfaceMap) {
		if (project !== null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (fileUnstableInterfaceMap !== null) {
				let fileUnstableInterfaceList = fileUnstableInterfaceMap[project.id];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Index</th>";
				html += "<th>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Instability</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Score</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Ingoing Dependencies</th>";
				html += "<th style='text-align: center; vertical-align: middle'>All Ingoing CoChange Files</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commits</th>";
				html += "</tr>";
				let index = 1;
				for(let fileIndex in fileUnstableInterfaceList) {
					if (fileUnstableInterfaceList.hasOwnProperty(fileIndex)) {
						let fileUnstableInterface = fileUnstableInterfaceList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + index + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + fileUnstableInterface.component.id + "'>" + fileUnstableInterface.component.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableInterface.component.instability).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (fileUnstableInterface.component.score).toFixed(2) + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableInterface.fanIn + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + fileUnstableInterface.coChangeFiles.length + "</td>";

						let allFilesIds = fileUnstableInterface.component.id;
						for(let j = 0; j < fileUnstableInterface.coChangeFiles.length; j++) {
							allFilesIds += "," + fileUnstableInterface.coChangeFiles[j].id;
						}

						html += "<td style='text-align: center; vertical-align: middle'>" + "<a target='_blank' href='/as/matrix?allFiles=" + allFilesIds + "&specifiedFiles=" + fileUnstableInterface.component.id + "&minCount=2'>commits</a>" + "</td>";
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
		unstableInterface: function(project, fileUnstableInterfaceMap, packageUnstableInterfaceMap) {
			_unstableInterface(project, fileUnstableInterfaceMap, packageUnstableInterfaceMap);
		}
	};
};
