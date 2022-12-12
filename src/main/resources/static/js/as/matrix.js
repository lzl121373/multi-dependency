var matrix = function() {
	
	var _matrix = function(matrixForCommits, matrixForIssues) {
		console.log(matrixForCommits.commits.length);
		console.log(matrixForIssues.issues.length);
		var html = "";
		html += "<table class='table table-bordered'>";
		html += "<tr>";
		html += "<td></td>";
		for(var i = 0; i < matrixForCommits.files.length; i++) {
			html += "<td><a href='/relation/file/" + matrixForCommits.files[i].id + "' target='_blank'>" + "(" + matrixForCommits.files[i].id + ") " + matrixForCommits.files[i].name + "</a></td>";
		}
		html += "</tr>";
		for(var i = 0; i < matrixForCommits.commits.length; i++) {
			html += "<tr>";
			html += "<td>" + (i + 1) +":<a target='_blank' href='/commit/" + matrixForCommits.commits[i].id + "'>" + matrixForCommits.commits[i].commitId + "</a></td>";
			for(var j = 0; j < matrixForCommits.files.length; j++) {
				html += "<td>";
				if(matrixForCommits.update[i][j] == true) {
					html += "T";
				}
				html += "</td>";
			}
			html += "</tr>";
		}
		for(var i = 0; i < matrixForIssues.issues.length; i++) {
			html += "<tr>";
			html += "<td><a target='_blank' href='/issue/" + matrixForIssues.issues[i].id + "'>" + "issue: " + matrixForIssues.issues[i].number + "</a></td>";
			for(var j = 0; j < matrixForIssues.files.length; j++) {
				html += "<td>";
				if(matrixForIssues.related[i][j] == true) {
					html += "T";
				}
				html += "</td>";
			}
			html += "</tr>";
		}
		 html += "</table>";
		$("#matrixForCommits").html(html);
	};
	
	return {
		matrix: function(matrixForCommits, matrixForIssues) {
			_matrix(matrixForCommits, matrixForIssues);
		}
	}
}
