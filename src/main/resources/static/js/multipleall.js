var multiple_microservice_all = function(cytoscapeutil) {
	var cyEntry = null;
	
	var firstTestCaseId = null;
	var secondTestCaseId = null;
	var toggleNode = function(id, checked) {
		var node = cyEntry.$("#" + id);
		if(checked) {
			cyEntry.$("#" + id).style({"visibility": "visible"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
//				var edge = node.connectedEdges()[i].data();
//				var self = sourceId == id ? edge.source : edge.target;
//				var other = sourceId == id ? edge.target : edge.source;
//				if(cyEntry.$("#" + other).visible()) {
//					cyEntry.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "visible"});
//				}
				cyEntry.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "visible"});
			}
		} else {
			cyEntry.$("#" + id).style({"visibility": "hidden"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
//				var edge = node.connectedEdges()[i].data();
//				var self = sourceId == id ? edge.source : edge.target;
//				var other = sourceId == id ? edge.target : edge.source;
//				if(cyEntry.$("#" + other).hidden()) {
//					cyEntry.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "hidden"});
//				}
				cyEntry.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "hidden"});
			}
		}
	}
	var nodeToPosition = new Map();
	var showZTree = function(nodes) {
		var setting = {
			callback: {
				onClick: function(event, treeId, treeNode) {
					var id = treeNode.id;
					console.log(id);
					if(id <= 0 || cyEntry == null) {
						return ;
					}
					var node = cyEntry.$('#' + id);
					cyEntry.fit(node, 350);
				},
				onCheck: function(event, treeId, treeNode) {
					var id = treeNode.id;
					var children = treeNode.children;
					if(children != null) {
						for(var i = 0; i < children.length; i++) {
							toggleNode(children[i].id, treeNode.checked);
						}
					} else{
						toggleNode(id, treeNode.checked);
					}
				}
			},
			check: {
				enable: true,
				chkStyle: "checkbox",
				chkboxType: { "Y" : "ps", "N" : "ps" }
			}
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init($("#ztree"), setting, zNodes);
	}

	var queryMultipleByTestCaseOrFeatureOrScenario = function(params, queryType) {
		var url = null;
		if(queryType == "TestCase") {
			url = "/multiple/all/testcase";
		} else if(queryType == "Feature") {
			url = "/multiple/all/feature";
		} else if(queryType == "Scenario") {
			url = "/multiple/all/scenario"; 
		}
		var nodes = cyEntry.nodes();
		for(var i = 0; i < nodes.length; i++) {
			var nodeId = nodes[i].data().id;
			var position = nodes[i].position();
			nodeToPosition.set(nodeId, position);
		}
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : url,
			data : JSON.stringify(params),
			success : function(result) {
				if (result.result == "success") {
					cyEntry.destroy();
					var nodes = result.value.data.nodes;
					for(var i = 0; i < nodes.length; i++) {
						var nodeId = nodes[i].data.id;
						var position = nodeToPosition.get(nodeId);
						nodes[i].position = position;
					}
					showZTree(result.value.ztreeNodes);
					cyEntry = cytoscapeutil.showDataInCytoscape($("#entry"), result.value.data, "preset");
					processCytoscape(cyEntry);
					setTapNode(cyEntry, result);
					if($("#showClonesInMicroService").prop('checked') == true) {
						$.ajax({
							type: 'GET',
							url: "/clone/microservice/line",
							success: function(result) {
								console.log(result);
								for(var id in result) {
									cyEntry.$("#" + id).data("height", 50);
									cyEntry.$("#" + id).data("name", result[id].project.name 
											+ "\n文件总行数：" + result[id].allFilesLines + "，文件数：" + result[id].allFiles.length
											+ "\n克隆相关文件行数：" + result[id].allCloneFilesLines + "，文件数：" + result[id].cloneFiles.length
											+ "\n克隆相关方法行数：" + result[id].allCloneFunctionsLines + "，方法数：" + result[id].cloneFunctions.length
									);
								}
							}
						});
					}
				}
			}
		});
	}
	
	var queryAll = function() {
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all",
			data : JSON.stringify({
				"showStructure" : $("#showStructure").prop('checked'),
				"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
				"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
				"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked')
			}),
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry = cytoscapeutil.showDataInCytoscape($("#entry"), result.value.data, "dagre");
					showZTree(result.value.ztreeNodes);
					processCytoscape(cyEntry);
					setTapNode(cyEntry, result);
					if($("#showClonesInMicroService").prop('checked') == true) {
						$.ajax({
							type: 'GET',
							url: "/clone/microservice/line",
							success: function(result) {
								console.log(result);
								for(var id in result) {
									cyEntry.$("#" + id).data("height", 50);
									cyEntry.$("#" + id).data("name", result[id].project.name 
											+ "\n文件总行数：" + result[id].allFilesLines + "，文件数：" + result[id].allFiles.length
											+ "\n克隆相关文件行数：" + result[id].allCloneFilesLines + "，文件数：" + result[id].cloneFiles.length
											+ "\n克隆相关方法行数：" + result[id].allCloneFunctionsLines + "，方法数：" + result[id].cloneFunctions.length
									);
								}
							}
						});
					}
				}
			}
		});
	}
	
	var queryEntryEdge = function(testCaseId, callChain = false) {
		
		var testCaseIds = [];
		
		if(firstTestCaseId == null) {
			firstTestCaseId = testCaseId;
			testCaseIds[testCaseIds.length] = firstTestCaseId;
		} else {
			secondTestCaseId = testCaseId;
			testCaseIds[testCaseIds.length] = firstTestCaseId;
			testCaseIds[testCaseIds.length] = secondTestCaseId;
		}
		
		var ids = {
				"ids" : testCaseIds,
				"callChain" : false
		};
		console.log(ids);
		$.ajax({
			type : "POST",
			contentType : "application/json",
			dataType : "json",
			url : "/multiple/all/microservice/query/edges",
			data : JSON.stringify(ids),
			success : function(result) {
				if (result.result == "success") {
					console.log(result);
					cyEntry.batch(function(){
						cyEntry.remove('edge[type="ShowStructureDependOnCall"]');
						cyEntry.remove('edge[type="ShowStructureDependOn"]');
						cyEntry.remove('edge[type="ShowStructureCall"]');
						cyEntry.remove('edge[type="NoStructureCall"]');
						cyEntry.remove('edge[type="TestCaseExecuteMicroService"]');
						cyEntry.remove('edge[type="all_MicroService_DependOn_MicroService"]');
						cyEntry.remove('edge[type="all_MicroService_call_MicroService"]');
						cyEntry.remove('edge[type="all_FeatureExecutedByTestCase"]');
						cyEntry.remove('edge[type="all_TestCaseExecuteMicroService"]');
						cyEntry.remove('edge[type="NewEdges"]');
						cyEntry.remove('edge[type="NewEdges_Edge1_Edge2"]');
						cyEntry.remove('edge[type="NewEdges_Edge1"]');
						cyEntry.remove('edge[type="NewEdges_Edge2"]');
					});
					var relatedNodes = result.nodes;
					var relatedEdges = result.edges;
					var datas = new Array();
					for(var i = 0; i < relatedEdges.length; i++) {
						cyEntry.remove(cyEntry.$("#" + relatedEdges[i].id));
						var data = {
								group: 'edges',
								data: {
//									type: "NewEdges",
//									id: relatedEdges[i].id,
									type: relatedEdges[i].type == null ? "NewEdges" : relatedEdges[i].type,
											source: relatedEdges[i].source,
											target: relatedEdges[i].target,
											value: relatedEdges[i].value
								}
						}
						datas.push(data);
					}
					cyEntry.add(datas);
					if(firstTestCaseId != null && secondTestCaseId != null) {
						firstTestCaseId = null;
						secondTestCaseId = null;
					}
				}
			}
		});
	}
	
	var processCytoscape = function(cyEntry) {
		var edges = new Array();
		var removeIds = new Array();
		for(var i = 0; i < cyEntry.edges().length; i++) {
			var type = cyEntry.edges()[i].data().type;
			if(type == "FeatureExecutedByTestCase") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "TestCaseExecuteFeature",
						source: target,
						target: source,
						value: ""
				};
				edges.push({data: edge});
			}
			if(type == "LibraryVersionIsFromLibrary") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "LibraryGroupAndNameContainVersion",
						source: target,
						target: source,
						value: ""
				};
				edges.push({data: edge});
			}
			if(type == "MicroServiceUpdatedByDeveloper") {
				var source = cyEntry.edges()[i].data().source;
				var target = cyEntry.edges()[i].data().target;
				removeIds.push(cyEntry.edges()[i].data().id);
				var edge = {
						type: "DeveloperUpdateMicroService",
						source: target,
						target: source,
						value: cyEntry.edges()[i].data().value
				};
				edges.push({data: edge});
			}
		}
		cyEntry.batch(function(){
			for(var i = 0; i < removeIds.length; i++){
				cytoscapeutil.removeEdge(cyEntry, removeIds[i]);
			}
			cytoscapeutil.addEdges(cyEntry, edges);
		});
	};
	
	var setTapNode = function(cyEntry, result) {
		cyEntry.on('tap', 'node', function(evt){
			var node = evt.target;
			if(node.data().type == "TestCase_success" || node.data().type == "TestCase_fail") {
				queryEntryEdge(node.data().id, true);
			}
		})
		cyEntry.on('tap', 'edge', function(evt){
			var edge = evt.target;
			if(edge.data().type == "all_MicroService_clone_MicroService") {
				var id = edge.data().id;
				var functions = result.cloneDetail[id];
				if(functions != null) {
					$("#table_clone").html("");
					var html = "";
					for(var i = 0; i < functions.length; i++) {
						html += "<tr>";
						html += "<td>";
						html += functions[i].function1.name + "<br/>" + functions[i].function2.name;
						html += "</td>";
						html += "</tr>";
					}
					$("#table_clone").html(html);
				}
			}
		});
	}
	
	var _multiselect = function() {
		$("#testCaseList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
			enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
		$("#featureList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
			enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
		$("#scenarioList").multiselect({
			enableClickableOptGroups: true,
			enableCollapsibleOptGroups: true,
			enableFiltering: true,
			collapseOptGroupsByDefault: true,
			enableCollapsibleOptGroups: true
		});
	};
	
	var _init = function(){
		_multiselect();
		$("#submitScenario").click(function() {
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#scenarioList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "Scenario");
		});
		$("#submitTestCase").click(function() {
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#testCaseList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "TestCase");
		});
		$("#submitFeature").click(function() {
			if(isNaN($("#showClonesMinPair").val())){
				alert($("#showClonesMinPair").val() + " 不是数字");
				return ;
			}
			var ids = {
					"ids" : $("#featureList").val(),
					"showStructure" : $("#showStructure").prop('checked'),
					"showMicroServiceCallLibs" : $("#showMicroServiceCallLibs").prop('checked'),
					"showClonesInMicroService" : $("#showClonesInMicroService").prop('checked'),
					"showCntOfDevUpdMs" : $("#showCntOfDevUpdMs").prop('checked'),
					"showClonesMinPair" : $("#showClonesMinPair").val()
			};
			console.log(ids);
			queryMultipleByTestCaseOrFeatureOrScenario(ids, "Feature");
		});
		
		$("#showImg").click(function() {
			cytoscapeutil.showImg(cyEntry, "entry-png-eg");
		})
	};
	var clearMemo = function() {
		$("#clearMemo").click(function() {
			if(cyEntry == null) {
				return ;
			}
			cyEntry = cytoscapeutil.refreshCy(cyEntry);
			processCytoscape(cyEntry);
			setTapNode(cyEntry, null);
		});
	};
	
	var showHistogram = function() {
		var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/microservice/fanIO",
			success : function(result) {
				// console.log(result);
				var xAxisData = [];
				var fanInData = [];
				var fanOutData = [];
				// console.log(result.length);
				for(var i = 0; i < result.length; i++) {
					xAxisData[i] = result[i].node.name;
					// console.log(xAxisData[i]);
					fanInData[i] = result[i].fanInSize;
					fanOutData[i] = result[i].fanOutSize;
				}

				var option = {
		        	    tooltip: {
		        	        trigger: 'axis',
		        	        axisPointer: {            // 坐标轴指示器，坐标轴触发有效
		        	            type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
		        	        }
		        	    },
		        	    legend: {
		        	        data: ['FAN_IN', 'FAN_OUT']
		        	    },
		        	    grid: {
		        	        left: '3%',
		        	        right: '4%',
		        	        bottom: '3%',
		        	        containLabel: true
		        	    },
		        	    xAxis: [
		        	        {
		        	            type: 'category',
		        	            data: xAxisData,
		        	            axisLabel: {  
		        	                interval:0,  
		        	                rotate:40  
		        	             }  
		        	        }
		        	    ],
		        	    yAxis: [
		        	        {
		        	            type: 'value'
		        	        }
		        	    ],
		        	    series: [
		        	        {
		        	            name: 'FAN_IN',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanInData
		        	        },
		        	        {
		        	            name: 'FAN_OUT',
		        	            type: 'bar',
		        	            stack: 'fan',
		        	            data: fanOutData
		        	        }
		        	    ]
		        	};
		        myChart.setOption(option);
			}
		});
	}
	
	var init = function(){
		_init();
		queryAll();
		clearMemo();
		showHistogram();
	};
	
	return {
		init : function() {
			init();
		}
	}
}

