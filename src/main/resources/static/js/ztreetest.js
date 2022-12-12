var ztreetest = function(cytoscapeutil) {

	var _init = function() {
		var zTreeObj;
		var setting = {
			check : {
				enable : true
			},
			data : {
				simpleData : {
					enable : true
				}
			}
		};

		var zNodes = [ {
			name : "父节点1",
			open: false,
			children : [ {
				name : "子节点1"
			}, {
				name : "子节点2"
			} ]
		} ];
		console.log(zNodes);
		zTreeObj = $.fn.zTree.init($("#treeDemo"), setting, zNodes);
	}

	return {
		init : function() {
			_init();
		}
	}
}
