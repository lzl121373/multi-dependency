var project = function(project, cytoscapeutil) {
	var cy = null;
	var showZTree = function(nodes, container = $("#ztree")) {
		var setting = {
		};
		var zNodes = nodes;
		var zTreeObj = $.fn.zTree.init(container, setting, zNodes);
	}
	var _project = function() {
		$('#project_select').multiselect({
			includeSelectAllOption: true
		});
		$("#search").click(function(){
			var dependency = $("input[name='dependency']:checked").val();
			var level = $("input[name='level']:checked").val();
			$.ajax({
				type: 'GET',
				url: "/project/cytoscape?projectId=" + project.id + "&dependency=" + dependency + "&level=" + level,
				success: function(result) {
					if(result.result == "success") {
						console.log(result.value);
						if(result.ztreenode != null) {
							showZTree(result.ztreenode);
						}
						var nodes = result.value.nodes;
						cy = cytoscapeutil.showDataInCytoscape($("#graph"), result.value, "dagre");
						cy.expandCollapse();
						var api = cy.expandCollapse('get');
						api.collapseAll();
						cy.layout({
							name: "dagre"
						}).run();
					} else {
						alert(result.msg);
					}
				}
			});
		});
		_showImg();
		_clearMemo();
		
		var myChart = echarts.init(document.getElementById('main'));
		$.ajax({
			type : "GET",
			url : "/project/fanIO/file/" + project.id,
			success : function(result) {
				console.log(result);
				var xAxisData = [];
				var fanInData = [];
				var fanOutData = [];
				console.log(result.length);
				for(var i = 0; i < result.length; i++) {
					xAxisData[i] = result[i].node.name;
					console.log(xAxisData[i]);
					fanInData[i] = result[i].fanIn.length == 0 ? null : result[i].fanIn.length;
					fanOutData[i] = result[i].fanOut.length == 0 ? null : result[i].fanOut.length;
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
		        // 使用刚指定的配置项和数据显示图表。
		        myChart.setOption(option);
			}
		});
	};
	var _showImg = function(){
		$("#showImg").click(function() {
			console.log("showImg");
			cytoscapeutil.showImg(cy, "entry-png-eg");
		})
	}
	var _clearMemo = function() {
		$("#clearMemo").click(function() {
			console.log("clearMemo");
			if(cy == null) {
				return ;
			}
			cy = cytoscapeutil.refreshCy(cy);
			cy.expandCollapse();
			var api = cy.expandCollapse('get');
			api.collapseAll();
			cy.layout({
				name: "dagre"
			}).run();
		});
	};
	
	return {
		init: function(){
			_project();
		}
	}
}

