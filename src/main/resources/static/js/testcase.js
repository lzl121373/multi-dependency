var cy = null;
var cyEntry = null;
var cyEntryInitElement = null;

var queryEntryEdge = function(testCaseId) {
	var testCaseIds = [];
	testCaseIds[testCaseIds.length] = testCaseId;
	var ids = {
			"ids" : testCaseIds
	};
	$.ajax({
		type : "POST",
		contentType : "application/json",
		dataType : "json",
		url : "/testcase/microservice/query/entry/edges",
		data : JSON.stringify(ids),
		success : function(result) {
			if (result.result == "success") {
				console.log(result);
				cyEntry.remove('node');
				cyEntry.remove('edge');
				cyEntry.add(cyEntryInitElement);
				var relatedEdges = result.edges;
				console.log(relatedEdges);
				var datas = new Array();
				for(var i = 0; i < relatedEdges.length; i++) {
					cyEntry.remove(cyEntry.$("#" + relatedEdges[i].id));
					var data = {
							group: 'edges',
							data: {
								type: "GREEN",
								id: relatedEdges[i].id,
								source: relatedEdges[i].source,
								target: relatedEdges[i].target
							}
					}
					datas.push(data);
				}
				cyEntry.add(datas);
				var relatedNodes = result.nodes;
				console.log(relatedNodes);
				for(var i = 0; i < cyEntry.nodes().length; i++) {
					if(cyEntry.nodes()[i].data().type == "Feature" || cyEntry.nodes()[i].data().type == "TestCase_success"
						|| cyEntry.nodes()[i].data().type == "TestCase_fail") {
						continue;
					}
					var temp = cyEntry.$("#" + cyEntry.nodes()[i].data().id).data();
					temp.type = "MicroService";
					cyEntry.$("#" + cyEntry.nodes()[i].data().id).data(temp);
				}
				for(var j = 0; j < relatedNodes.length; j++) {
					var temp = cyEntry.$("#" + relatedNodes[j].id).data();
					temp.type = "MicroService_related";
					cyEntry.$("#" + relatedNodes[j].id).data(temp);
				}
				/*for(var i = 0; i < cyEntry.nodes().length; i++) {
						if(cyEntry.nodes()[i].data().type == "Feature" || cyEntry.nodes()[i].data().type == "TestCase_success"
							 || cyEntry.nodes()[i].data().type == "TestCase_fail") {
							continue;
						}
						for(var j = 0; j < relatedNodes.length; j++) {
							if(cyEntry.nodes()[i].data().id == relatedNodes[j].id) {
								console.log(cyEntry.nodes()[i].data().id + " " + relatedNodes[j].id);
								var temp = cyEntry.$("#" + relatedNodes[j].id).data();
								temp.type = "MicroService_related";
								cyEntry.$("#" + cyEntry.nodes()[i].data().id).data(temp);
							} else {
								var temp = cyEntry.$("#" + cyEntry.nodes()[i].data().id).data();
								temp.type = "MicroService";
								cyEntry.$("#" + cyEntry.nodes()[i].data().id).data(temp);
							}
						}
					}*/
				
				
			}
		}
	});
}
var queryEntry = function(testCaseIds) {
	cyEntry = null;
	cyEntryInitElement = null;
	$.ajax({
		type : "POST",
		contentType : "application/json",
		dataType : "json",
		url : "/testcase/microservice/query/entry",
		data : JSON.stringify(testCaseIds),
		success : function(result) {
			if (result.result == "success") {
				cyEntry = showDataInCytoscape($("#entry"), result.value.value, "dagre");
				console.log(cyEntry.elements());
				if(cyEntryInitElement == null) {
					cyEntryInitElement = cyEntry.elements();
				}
				cyEntry.on('tap', 'node', function(evt){
					var node = evt.target;
					if(node.data().type != "TestCase_success" && node.data().type != "TestCase_fail") {
						return ;
					}
					queryEntryEdge(node.data().id);
				})
			}
		}
	});
}
var queryTestCase = function(testCaseIds) {
	$.ajax({
		type : "POST",
		contentType : "application/json",
		dataType : "json",
		url : "/testcase/microservice/query/union",
		data : JSON.stringify(testCaseIds),
		success : function(result) {
			console.log(result);
			if (result.result == "success") {
				cy = showDataInCytoscape($("#all"), result.coverageValue.value, "dagre");
				var html = "";
				for(var i = 0; i < result.testCases.length; i++) {
					html += "<a href='#' value='" + i + "' class='query_entry' name='" + result.testCases[i].testCaseId + "'>" + result.testCases[i].name + "</a>"
					if(i != result.testCases.length - 1) {
						html += "<span>、</span>";
					}
				}
				$("#testCaseTitle").html(html);
			}
		}
	});
};

var _init = function(){
	$("#testCaseList").multiselect({
		enableClickableOptGroups: true,
		enableCollapsibleOptGroups: true,
		enableFiltering: true,
		collapseOptGroupsByDefault: true,
		enableCollapsibleOptGroups: true
	});
	
	$("#submit").click(function() {
		var ids = {
				"ids" : $("#testCaseList").val()
		};
		console.log(ids);
		queryTestCase(ids);
		console.log(ids);
		queryEntry(ids);
	});
	
	$("#showImg").click(function() {
		if(cy != null) {
			$('#png-eg').attr('src', cy.png({
				bg: "#ffffff",
				full : true
			}));
			$('#png-eg').css("background-color", "#ffffff");
		}
		if(cyEntry != null) {
			$('#entry-png-eg').attr('src', cyEntry.png({
				bg: "#ffffff",
				full : true
			}));
			$('#entry-png-eg').css("background-color", "#ffffff");
		}
	})
	
};
var init = function(){
	_init();
}
