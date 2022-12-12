var rType = function(typeId, cytoscapeutil) {
	var _type = function() {
		containFunction(typeId);
		containField(typeId);
		superExtends(typeId);
		subExtends(typeId);
		superImplements(typeId);
		subImplements(typeId);
	};
	
	var superExtends = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/extends/super",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#extends_content_superType").html(html);
			}
		});
	}
	
	var subExtends = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/extends/sub",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#extends_content_subType").html(html);
			}
		});
	}
	
	var superImplements = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/implements/super",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#implements_content_superType").html(html);
			}
		});
	}
	
	var subImplements = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/implements/sub",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/type/" + result[i].id + "' >";
					html += result[i].name;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#implements_content_subType").html(html);
			}
		});
	}
	
	var containFunction = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/contain/function",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/function/" + result[i].id + "' >";
					html += result[i].name + result[i].parametersIdentifies;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#contain_function_content").html(html);
			}
		});
	}
	
	var containField = function(typeId) {
		$.ajax({
			type: "get",
			url: "/relation/type/" + typeId + "/contain/field",
			success: function(result) {
				var html = "<ol>";
				console.log(result);
				for(var i = 0; i < result.length; i++) {
					html += "<li><a target='_blank' href='/relation/field/" + result[i].id + "' >";
					html += result[i].name + " : " + result[i].typeIdentify;
					html += "</a></li>";
				}
				html += "</ol>";
				$("#contain_field_content").html(html);
			}
		});
	}
	
	return {
		init: function(){
			_type();
		}
	}
}

