function copyToClip(content) {
    var aux = document.createElement("input"); 
    aux.setAttribute("value", content); 
    document.body.appendChild(aux); 
    aux.select();
    document.execCommand("copy"); 
    document.body.removeChild(aux);
//    alert("复制成功");
}
var clone = function(cytoscapeutil) {
	var param = {
		searchCloneRelationTypeSelect : null,
		language: null,
		filter: null
	}
	var initParam = function() {
		param = {
			searchCloneRelationTypeSelect : null,
			language: null,
			filter: null
		}
	}
	// var histogramProjectsSizeChart = echarts.init(document.getElementById('projects_size_histogram'));
	// var histogramChart = echarts.init(document.getElementById('main'));
	var mainUrl = "/clonegroup";
	var isFullScreen = false;
	function showFull(divId){
		var full=document.getElementById(divId);
		launchIntoFullscreen(full);
		isFullScreen = true;
		$(".div_cytoscape").css("height", "100%");
		$(".div_cytoscape_content").css("height", "100%");
		$(".div_cytoscape_treeview").css("height", "100%");
	}
	function delFull() {
		exitFullscreen();
	}
	window.onresize = function() {
		if(isFullScreen == true) {
			isFullScreen = false;
		} else {
			$(".div_cytoscape").css("height", "500px");
			$(".div_cytoscape_content").css("height", "500px");
			$(".div_cytoscape_treeview").css("height", "500px");
		}
	};
	var toggleNode = function(id, checked, cy) {
		var node = cy.$("#" + id);
		if(node == null) {
			return;
		}
		if(checked) {
			cy.$("#" + id).style({"visibility": "visible"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
				cy.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "visible"});
			}
		} else {
			cy.$("#" + id).style({"visibility": "hidden"});
			for(var i = 0; i < node.connectedEdges().length; i++) {
				cy.$("#" + node.connectedEdges()[i].data().id).style({"visibility": "hidden"});
			}
		}
	}
	var cys = [];
	var showZTree = function(nodes, container, cy, copyDivId = "") {
		var setting = {
				callback: {
					onClick: function(event, treeId, treeNode) {
						var id = treeNode.id;
						if(id <= 0 || cy == null) {
							return ;
						}
						var node = cy.$('#' + id);
						if(node != null) {
							console.log(node.data());
							cy.fit(node);
						}
						var html = "<table class='table table-bordered'>";
						var children = treeNode.children;
						html += "<tr>";
						html += "<td><a class='clipBoard'>";
						html += treeNode.name;
						html += "</a></td>";
						if(children != null) {
							html += "<td>";
							for(var i = 0; i < children.length; i++) {
								html += "<a class='clipBoard'>" + id + " " + children[i].name + "</a></br>";
							}
							html += "</td>";
						}
						html += "</tr>";
						html += "</table>";
						console.log(children);
						$("#" + copyDivId).html(html);
						$(".clipBoard").click(function(){
							var content = $(this).text();
							copyToClip(content)
						});
					},
					onCheck: function(event, treeId, treeNode) {
						var id = treeNode.id;
						toggleNode(id, treeNode.checked, cy);
					}
				},
				check: {
					enable: true,
					chkStyle: "checkbox",
					chkboxType: { "Y" : "", "N" : "" }
				}
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var ellipseStyle = {
			'shape' : 'ellipse',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
				for(var i = 0; i < split.length; i++) {
					var width = split[i].replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
					if(width > maxWidth) {
						maxWidth = width;
					}
				}
				return maxWidth;
			},
			'height': function(content) {
				var split = content.data().name.split("\n");
				var length = split.length;
				return 21 * length;
			},
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)',
			'text-wrap': 'wrap'
		}
	var _showCytoscape = function(container, data, copyDivId = "") {
		var cy = cytoscapeutil.showDataInCytoscape(container, data, "random");
		if(copyDivId != "") {
			cy.on('tap', 'node', function(evt){
				var node = evt.target;
				if(node.data().type == "File") {
					var value = node.data().value;
					var html = "<a class='clipBoard'>";
					html += value;
					html += "</a>";
					$("#" + copyDivId).html(html);
					$(".clipBoard").click(function(){
						var content = $(this).text();
						copyToClip(content)
					});
				} else if(node.data().type == "Function" || node.data().type == "Type" || node.data().type == "Snippet") {
					var value = node.data().value;
					var html = "<a class='clipBoard'>";
					html += value;
					html += "</a>";
					html += "&nbsp;&nbsp;from&nbsp;&nbsp;";
					var connect = node.connectedEdges();
					for(var i = 0; i < connect.length; i++) {
						if(connect[i].data().type == "Contain") {
							html += "<a class='clipBoard'>";
							html += cy.$("#" + connect[i].data().source).data().value;
							html += "</a>";
						}
					}
					$("#" + copyDivId).html(html);
					$(".clipBoard").click(function(){
						var content = $(this).text();
						copyToClip(content)
					});
				}
			})
			cy.on('tap', 'edge', function(evt){
				var edge = evt.target;
				console.log(edge.data);
				$(".clipBoard").click(function(){
					var content = $(this).text();
					console.log(content);
					copyToClip(content)
				});
			})
		}
		cys[cys.length] = cy;
		cy.style().selector('node[type="Type"]').style(ellipseStyle).update();
		cy.style().selector('node[type="Function"]').style(ellipseStyle).update();
		cy.style().selector('node[type="Snippet"]').style(ellipseStyle).update();
		cy.style().selector('node[type="CloneGroup"]').style({
			'shape' : 'rectangle',
			'width': function(content) {
				var split = content.data().name.split("\n");
				var maxWidth = 0;
				for(var i = 0; i < split.length; i++) {
					var width = split[i].replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
					if(width > maxWidth) {
						maxWidth = width;
					}
				}
				return maxWidth;
			},
			'height': function(content) {
				var split = content.data().name.split("\n");
				var length = split.length;
				return 21 * length;
			},
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)',
			'text-wrap': 'wrap'
		}).update();
		cy.style().selector('node[type="File"]').style(ellipseStyle).update();
		cy.style().selector('node[type="Project"]').style({
			'shape' : 'rectangle',
			'width': function(content) {
				return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
			},
			'height': 30,
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#9af486',
			'content': 'data(name)'
		}).update();
		cy.style().selector('node[type="Package"]').style({
			'shape' : 'rectangle',
			'width': function(content) {
				return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
			},
			'height': 30,
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)'
		}).update();
		cy.style().selector('node[type="MicroService"]').style({
			'shape' : 'hexagon',
			'width': function(content) {
				return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
			},
			'height': 30,
			'text-valign': 'center',
			'text-halign': 'center',
			'border-width': 1.5,
			'border-color': '#555',
			'background-color': '#f6f6f6',
			'content': 'data(name)'
		}).update();
		cy.style().selector('edge[type="Clone"]').style({
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'line-style': 'dashed',
			'target-arrow-shape' : 'none',
			'font-size' : 20
		}).update();
		var edges = cy.remove('edge[type="Clone"]');
//		cy.layout({name : 'dagre'}).run();
		cy.layout({
			name : "concentric",
			concentric: function( node ){
//		          return node.degree();
					if(node.data().type == "CloneGroup") {
						return 300;
					} else if(node.data().type == "Function" || node.data().type == "Type" || node.data().type == "Snippet") {
						return 200;
					} else if(node.data().type == "File") {
						return 100;
					} else if(node.data().type == "MicroService") {
						return 2;
					} 
					return 1;
		        },
		        levelWidth: function( nodes ){
		          return 3;
		        }
		}).run();
		cy.add(edges);
		return cy;
	}
	var _clone = function(cloneGroupName) {
		/*
		为文件克隆添加cochange值
		by Kinsgley
		2020/07/24
		*/
		var showClonesTable = function(clonesWithCoChange, divId) {
			console.log(clonesWithCoChange);
			if(clonesWithCoChange.length == 0) {
				return ;
			}
			var html = "<table class='table table-bordered'>";
			html += "<tr><th>file1</th><th>file2</th><th>type</th><th>value</th><th>cochange</th></tr>";
			for(var i = 0; i < clonesWithCoChange.length; i++) {
				var cochangeId = clonesWithCoChange[i].cochange == null ? -1 : clonesWithCoChange[i].cochange.id;
				html += "<tr>";
				var linesSize1 = clonesWithCoChange[i].fileClone.linesSize1;
                var linesSize2 = clonesWithCoChange[i].fileClone.linesSize2;
				html += "<td>" + clonesWithCoChange[i].file1.path + "(" + linesSize1 + ")";
				html += "</td>";
				html += "<td>" + clonesWithCoChange[i].file2.path + "(" + linesSize2 + ")";
				html += "</td>";
				html += "<td>" ;
				var type = clonesWithCoChange[i].fileClone.cloneType;
				var loc1 = clonesWithCoChange[i].fileClone.loc1;
				var loc2 = clonesWithCoChange[i].fileClone.loc2;
				var value = clonesWithCoChange[i].fileClone.value;
				html += "<a target='_blank' href='/clone/file/double" +
					"?file1Id=" + clonesWithCoChange[i].file1.id +
					"&file2Id=" + clonesWithCoChange[i].file2.id +
					"&cloneType=" + type +
					"&linesSize1=" + linesSize1 +
					"&linesSize2=" + linesSize2 +
					"&loc1=" + loc1 +
					"&loc2=" + loc2 +
					"&value=" + value +
					"&cochange=" + clonesWithCoChange[i].cochangeTimes +
					"&filePath1=" + clonesWithCoChange[i].file1.path +
					"&filePath2=" + clonesWithCoChange[i].file2.path +
					"&cochangeId=" + cochangeId +
					"'>" + type + "</a>";
				html += "</td>";
				html += "<td>" ;
				html += "<a target='_blank' href='/clone/compare?id1=" + clonesWithCoChange[i].file1.id + "&id2=" + clonesWithCoChange[i].file2.id + "'>" + clonesWithCoChange[i].fileClone.value + "</a>";
				html += "</td>";
				html += "<td>" ;
				html += "<a class='cochangeTimes' target='_blank' href='/commit/cochange?cochangeId=" + cochangeId + "'>" + clonesWithCoChange[i].cochangeTimes + "</a>";
				html += "</td>";
				html += "</tr>";
			}
			html += "</table>";
			$("#" + divId).html(html);
		};

		$.ajax({
			type : "GET",
			url : mainUrl + "/cytoscape/" + cloneGroupName + "?singleLanguage=true",
			success : function(result) {
				if(result.result === "success") {
					console.log(result.value);
					var html = "";
					html += "<div class='col-sm-12'><button class='btn btn-default fullscreen_btn'>全屏</button>";
					html += "<p></p></div>";
					html += "<div class='col-sm-12'><a target='_blank' href='/relation/dependsdetail/" + result.group.name + "'><h4>" + result.group.name + "</h4></a></div>"
					html += '<div class="col-sm-12 div_cytoscape_div" id="fullscreenAble">';
					html += '<div class="div_cytoscape_treeview">';
					html += '<ul id="node_ztree_num" class="ztree"></ul>';
					html += '</div>';
					html += '<div class="div_cytoscape" style="float: left; display: inline;">';
					html += '<div id="cloneGroupDiv" class="div_cytoscape_content cy"></div>';
					html += '</div>'
					html += '</div>';
					html += '<div class="col-sm-12" id="copyDiv_group_one"></div>';
					html += '<div class="col-sm-12" id="table_clones_one"></div>';
					html += '<svg class="col-sm-12" id="chart_clones_one"></svg>';
					html += '<div class="col-sm-12"><hr/></div>';
					$("#specifiedCytoscape").html(html);
					$(".fullscreen_btn").unbind("click");
					$(".fullscreen_btn").click(function(){
						showFull("fullscreenAble");
					})
					var cy = _showCytoscape($("#cloneGroupDiv"), result.value, "copyDiv_group_one");
					showZTree(result.value.ztree, $("#node_ztree_num"), cy, "copyDiv_group_one");
					showClonesTable(result.value.clonesWithCoChange, "table_clones_one");
					$.ajax({
						type: "get",
						url: mainUrl + "/cytoscape/double/json?clonegroupName=" + cloneGroupName,
						success: function(result) {
							console.log(result);
							cloneGroupToGraph2(result.result, "chart_clones_one");
						}
					});
				}
			}
		});

	};

	return {
		init: function(cloneGroupName){
			_clone(cloneGroupName);
		}
	}
}
