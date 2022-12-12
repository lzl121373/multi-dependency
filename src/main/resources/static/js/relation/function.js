var rFunction = function(functionId, cytoscapeutil) {
	var _function = function() {
		call(functionId);
	};
	
	var call = function(functionId) {
		$.ajax({
			type: "get",
			url: "/relation/function/" + functionId + "/call",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/function/" + result[i].callFunction.id + "' >";
					html += result[i].callFunction.name;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#call_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_function();
		}
	}
}

