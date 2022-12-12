let multipleSmell = function(project, multipleSmellASFileMap, multipleSmellASPackageMap) {
	let _multipleSmell = function() {
		if (project != null) {
			let html = "";
			html += "<div>";
			html += "<div>";
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			html += "</div>";

			if (multipleSmellASFileMap != null) {
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>ID</th>";
				html += "<th style='vertical-align: middle'>File</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Cyclic Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Hub-Like Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Unstable Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Unstable Interface</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Implicit Cross Module Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Unutilized Abstraction</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Unused Include</th>";
				html += "</tr>";
				let multipleSmellASFileList = multipleSmellASFileMap[project.id];
				for(let fileIndex in multipleSmellASFileList) {
					if (multipleSmellASFileList.hasOwnProperty(fileIndex)) {
						let multipleSmellASFile = multipleSmellASFileList[fileIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + multipleSmellASFile.file.id + "</td>";
						html += "<td><a target='_blank' href='/relation/file/" + multipleSmellASFile.file.id + "'>" + multipleSmellASFile.file.path + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.cyclicDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.hubLikeDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.unstableDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.unstableInterface === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.implicitCrossModuleDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.unutilizedAbstraction === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASFile.unusedInclude === true ? "T" : "") + "</td>";
						html += "</tr>";
					}
				}
				html += "</table>";
				html += "</div>";
			}

			if (multipleSmellASPackageMap != null) {
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>ID</th>";
				html += "<th style='vertical-align: middle'>Package</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Cyclic Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Hub-Like Dependency</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Implicit Cross Module Dependency</th>";
				html += "</tr>";
				let multipleSmellASPackageList = multipleSmellASPackageMap[project.id];
				for(let packageIndex in multipleSmellASPackageList) {
					if (multipleSmellASPackageList.hasOwnProperty(packageIndex)) {
						let multipleSmellASPackage = multipleSmellASPackageList[packageIndex];
						html += "<tr>";
						html += "<td style='text-align: center; vertical-align: middle'>" + multipleSmellASPackage.pck.id + "</td>";
						html += "<td><a target='_blank' href='/relation/package/" + multipleSmellASPackage.pck.id + "'>" + multipleSmellASPackage.pck.directoryPath + "</a></td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASPackage.cyclicDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASPackage.hubLikeDependency === true ? "T" : "") + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + (multipleSmellASPackage.implicitCrossModuleDependency === true ? "T" : "") + "</td>";
						html += "</tr>";
					}
				}
				html += "</table>";
				html += "</div>";
			}

			html += "</div>";
			$("#content").html(html);
		}
	}
	
	return {
		init : function() {
			_multipleSmell();
		}
	}
}
