var god = function(cytoscapeutil) {
	var _god = function(projects, files, packages) {
		var html = "";

		for(var projectIndex in projects) {
			var project = projects[projectIndex];
			html += "<h4>" + project.name + " (" + project.language + ")</h4>";
			var unstableFiles = files[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>File</th>";
			html += "<th>LOC</th>";
			html += "</tr>";
			for(var fileIndex in unstableFiles) {
				var file = unstableFiles[fileIndex];
				console.log(file);
				html += "<tr>";
				html += "<td><a target='_blank' href='/relation/file/" + file.file.id + "'>" + file.file.path + "</a></td>";
				html += "<td>" + file.metrics.loc + "</td>";
				html += "</tr>";
			}
			html += "</table>";
			
			var godPackages = packages[project.id];
			html += "<table class='table table-bordered'>";
			html += "<tr>";
			html += "<th>Package</th>";
			html += "<th>NOF</th>";
			html += "</tr>";
			for(var packageIndex in godPackages) {
				var pck = godPackages[packageIndex];
				console.log(pck);
				html += "<tr>";
				html += "<td>" + pck.pck.directoryPath + "</td>";
				html += "<td>" + pck.pckMetrics.nof + "</td>";
				html += "</tr>";
			}
			
			html += "</table>";
		}
		
		$("#content").html(html);
	}
	
	var _save = function() {
		var setThreshold = function(projectId, minFileLoc, minFileCountInPackage) {
			$.ajax({
				type: "post",
				url: "/as/god/threshold/" + projectId 
					+ "?minFileLoc=" + minFileLoc
					+ "&minFileCountInPackage=" + minFileCountInPackage,
				success: function(result) {
					if(result == true) {
						alert("修改成功");
					} else {
						alert("修改失败");
					}
				}
			});
		};
		$("#godThresholdSave").click(function() {
			var minFileLOCThreshold = $("#minFileLOCThreshold").val();
			var minFileCountInPackageThreshold = $("#minFileCountInPackageThreshold").val();
			var projectId = $("#godComponentsProjects").val();
			setThreshold(projectId, minFileLOCThreshold, minFileCountInPackageThreshold);
		});
	}
	
	var _get = function() {
		var getThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/god/threshold/" + projectId,
				success: function(result) {
					console.log(result);
					$("#minFileLOCThreshold").val(result[0]);
					$("#minFileCountInPackageThreshold").val(result[1]);
				}
			})
		}
		$("#godComponentsProjects").change(function() {
			getThreshold($(this).val())
		})
		if($("#godComponentsProjects").val() != null) {
			getThreshold($("#godComponentsProjects").val());
		}
		
	}
	
	return {
		init : function() {
			_save();
			_get();
		},
		god: function(projects, godFiles, godPackages) {
			_god(projects, godFiles, godPackages);
		}
	}
}
