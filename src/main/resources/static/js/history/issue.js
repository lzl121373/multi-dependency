var issue = function(issueId, cytoscapeutil) {
	var _issue = function() {
		relatedFiles(issueId);
		relatedCommits(issueId);
	};
	
	var relatedCommits = function(issueId) {
		$.ajax({
			type: "get",
			url: "/issue/" + issueId + "/commits",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/commit/" + result[i].id + "' >";
					html += result[i].commitId;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#commit_content").html(html);
			}
		});
	}
	
	var relatedFiles = function(issueId) {
		$.ajax({
			type: "get",
			url: "/issue/" + issueId + "/files",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/file/" + result[i].id + "' >";
					html += result[i].path;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#file_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_issue();
		}
	}
}

