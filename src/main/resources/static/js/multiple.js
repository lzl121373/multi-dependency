var multiple = function(cytoscapeutil, ztree) {
	console.log(ztree);
	var _multiple = function() {
		var zTreeObj;
//		var setting = {};
		var setting = {
				check: {
					enable: true
				},
				data: {
					simpleData: {
						enable: true
					}
				}
		};

		var zNodes = ztree;
		console.log(zNodes);
		zTreeObj = $.fn.zTree.init($("#treeDemo"), setting, zNodes);
	}

	return {
		init : function() {
			_multiple();
		}
	}
}
