	
	var _showTreeView = function(containerDivId, data) {
		containerDivId.treeview({
			data : data,
			showTags : true,
			levels: 1
		});
	};
	
	var showTreeView = function(containerDivId, data) {
		_showTreeView(containerDivId, data)
	};
