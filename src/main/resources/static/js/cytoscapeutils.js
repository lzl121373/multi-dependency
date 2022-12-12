var cytoscapeutil = function() {
	var _showTreeView = function(containerDivId, data) {
		containerDivId.treeview({
			data : data,
			showTags : true,
			levels: 1
		});
	};
	var styleEdgeBlue = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'blue',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'blue',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeBlack = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'black',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'black',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeGreen = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'green',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeRed = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'red',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'red',
			'line-style': 'solid',
			'font-size' : 20
	};
	var styleEdgeDashed = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'black',
			'target-arrow-shape': 'triangle',
			'target-arrow-color': 'black',
			'line-style': 'dashed',
			'font-size' : 20
	};
	var styleEdgeClone = {
			'content': 'data(value)',
			'curve-style': 'bezier',
			'width': 1,
			'line-color': 'green',
			'line-style': 'dashed',
			'target-arrow-shape' : 'none',
			'font-size' : 20
	}
	// $("#containerId")
	var _showDataInCytoscape = function(container, elements, layout="dagre") {
		console.log("_showDataInCytoscape: " + layout);
		var cy = cytoscape({
			container: container,
			layout: {
				name: layout
			},
			textureOnViewport: false,
			hideEdgesOnViewport: true,
			motionBlurOpacity: true,
			boxSelectionEnabled: true,
//	    	pixelRatio: 1,
			style: [
				{
					selector: 'node',
					style: {
						'shape' : 'rectangle',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.length * 13;
						},
						'height': 30,
						'background-color': '#00FF66',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Package"]',
					style: {
						'shape' : 'rectangle',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.length * 13;
						},
						'height': 30,
						'text-valign': 'top',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="File"]',
					style: {
						'shape' : 'rectangle',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.length * 13;
						},
						'height': 30,
						'text-valign': 'top',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Type"]',
					style: {
						'shape' : 'rectangle',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.length * 13;
						},
						'height': 30,
						'text-valign': 'top',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Function"]',
					style: {
						'shape' : 'rectangle',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Variable"]',
					style: {
						'shape' : 'ellipse',
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
					}
				},
				{
					selector: 'node[type="Feature"]',
					style: {
						'shape' : 'ellipse',
						'width' :  function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 9;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Scenario"]',
					style: {
						'shape' : 'rectangle',
						'width':  function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="TestCase"]',
					style: {
						'shape' : 'rectangle',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="TestCase_success"]',
					style: {
						'shape' : 'rectangle',
						'width' : function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#9EEA6A',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="TestCase_fail"]',
					style: {
						'shape' : 'rectangle',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 8;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': 'red',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="MicroService"]',
					style: {
						'shape' : 'hexagon',
						'width': function(content) {
//							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
							var split = content.data().name.split("\n");
							var maxWidth = 0;
							for(var i = 0; i < split.length; i++) {
								var width = split[i].replace(/[^\u0000-\u00ff]/g,"aa").length * 14;
								if(width > maxWidth) {
									maxWidth = width;
								}
							}
							return maxWidth;
						},
						'height': function(content) {
							var split = content.data().name.split("\n");
							var length = split.length;
							return 17 * length;
						},
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)',
						"text-wrap": "wrap"
//					      "text-max-width": 80
					}
				},
				{
					selector: 'node[type="MicroServiceWithRestfulAPI"]',
					style: {
						'shape' : 'hexagon',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
						},
						'height': 30,
						'text-valign': 'top',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="MicroService_related"]',
					style: {
						'shape' : 'hexagon',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#00FF66',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Library"]',
					style: {
						'shape' : 'ellipse',
//	    				'width': 'data(length)',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#00FF66',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Developer"]',
					style: {
						'shape' : 'ellipse',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
						},
						'height': 30,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#FFFFFF',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="Entry"]',
					style: {
						'shape' : 'ellipse',
						'width': 50,
						'height': 25,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'node[type="API"]',
					style: {
						'shape' : 'ellipse',
						'width': function(content) {
							return content.data().name.replace(/[^\u0000-\u00ff]/g,"aa").length * 10;
						},
						'height': 25,
						'text-valign': 'center',
						'text-halign': 'center',
						'border-width': 1.5,
						'border-color': '#555',
						'background-color': '#f6f6f6',
						'content': 'data(name)'
					}
				},
				{
					selector: 'edge',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="TestCaseExecuteMicroService"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="ShowStructureDependOnCall"]',
					style: styleEdgeGreen
				},
				{
					selector: 'edge[type="ShowStructureDependOn"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="ShowStructureCall"]',
					style: styleEdgeRed
				},
				{
					selector: 'edge[type="NoStructureCall"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="NewEdges"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="NewEdges_Edge1_Edge2"]',
					style: styleEdgeBlue
				},
				{
					selector: 'edge[type="NewEdges_Edge1"]',
					style: styleEdgeGreen
				},
				{
					selector: 'edge[type="NewEdges_Edge2"]',
					style: styleEdgeRed
				},
				{
					selector: 'edge[type="all_Feature_Contain_Feature"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="all_ScenarioDefineTestCase"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="all_TestCaseExecuteMicroService"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="all_FeatureExecutedByTestCase]"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="all_MicroService_call_MicroService"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="all_MicroService_DependOn_MicroService"]',
					style: styleEdgeDashed
				},
				{
					selector: 'edge[type="all_MicroService_clone_MicroService"]',
					style: styleEdgeClone
				},
				{
					selector: 'edge[type="DeveloperUpdateMicroService"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="APICall"]',
					style: styleEdgeBlack
				},
				{
					selector: 'edge[type="order"]',
					style: styleEdgeRed
				},
				{
					selector: 'edge[type="contain"]',
					style: {
						'content': 'data(value)',
						'curve-style': 'bezier',
						'width': 1,
						'line-color': 'red',
						'target-arrow-shape': 'triangle',
						'target-arrow-color': 'red'
					}
				},
				{
					selector: 'edge[type="Function_clone_Function"]',
					style: styleEdgeClone
				}
				],
				elements: elements
		});
		cy.panzoom();
		return cy;
	};
	
	var _addNodes = function(cy, nodes) {
		for(var i = 0; i < nodes.length; i++) {
			console.log(nodes[i]);
			cy.add({data: nodes[i].data})
		}
		return cy;
	};
	var _addEdges = function(cy, edges) {
		for(var i = 0; i < edges.length; i++) {
			var data = edges[i].data;
//			data["line-color"] = 'yellow';
			cy.add({data: data})
		}
		return cy;
	};
	
	var _removeEdge = function(cy, edgeId) {
		cy.remove(cy.$("#" + edgeId));
		return cy;
	};
	
	var _removeNode = function(cy, nodeId) {
		cy.remove(cy.$("#" + nodeId));
		return cy;
	};
	
	var _refresh = function(cy) {
		var cyNodes = cy.nodes();
		console.log(cyNodes)
		var cyEdges = cy.edges();
		var newNodes = [];
		var newEdges = [];
		var value = {};
		for(var i = 0; i < cyNodes.length; i++) {
			console.log(cyNodes[i].data());
			console.log(cyNodes[i].position());
			newNodes[i] = {};
			newNodes[i].data = cyNodes[i].data();
			newNodes[i].position = cyNodes[i].position();
		}
		for(var i = 0; i < cyEdges.length; i++) {
			console.log(cyEdges[i].data());
			newEdges[i] = {};
			newEdges[i].data = cyEdges[i].data();
		}
		value.nodes = newNodes;
		value.edges = newEdges;
		console.log(value);
		
		cy = _showDataInCytoscape($("#" + cy.container().id), value, "preset");
		return cy;
	};
	

	return {
		showImg : function(cy, imgContainerId) {
			if(cy != null) {
				console.log("showImg");
				$("#" + imgContainerId).attr('src', cy.png({
					bg: "#ffffff",
					full : true
				}));
				$("#" + imgContainerId).css("background-color", "#ffffff");
			}
			return cy;
		},
		refreshCy: function(cy) {
			return _refresh(cy);
		},
		showDataInCytoscape: function(container, elements, layout) {
			return _showDataInCytoscape(container, elements, layout);
		},
		removeEdge: function(cy, edgeId) {
			return _removeEdge(cy, edgeId);
		},
		showTreeView: function(containerDivId, data) {
			_showTreeView(containerDivId, data)
		},
		addNodes: function(cytoscape, nodes) {
			return _addNodes(cytoscape, nodes);
		},
		addEdges: function(cytoscape, edges) {
			return _addEdges(cytoscape, edges);
		}
	}
}
