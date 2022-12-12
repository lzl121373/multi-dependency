var clone = function(cytoscapeutil) {
	var packagesClone = function() {
		$.ajax({
			type: "get",
			url: "/clone/package",
			success:function(result) {
				console.log("success");
				var html = "<div><span>" + result.length + "</span></div>";
				html += "<table class='table table-bordered'>";
				html += "<tr><th>序号</th><th>目录1</th><th>目录2</th><th>克隆文件占比</th><th>克隆Loc占比</th><th>克隆CoChange占比</th><th>克隆相似度</th><th>type</th><th>克隆对数</th></tr>";
				for(var i = 0; i < result.length; i++) {
					var clonePairs = result[i].clonePairs;
					var cloneNodesCount1 = result[i].cloneNodesCount1;
					var cloneNodesCount2 = result[i].cloneNodesCount2;
					var allNodesCount1 = result[i].allNodesCount1;
					var allNodesCount2 = result[i].allNodesCount2;
					var cloneMatchRate = 0.00;
					var cloneNodesLoc1 = result[i].cloneNodesLoc1;
					var cloneNodesLoc2 = result[i].cloneNodesLoc2;
					var allNodesLoc1 = result[i].allNodesLoc1;
					var allNodesLoc2 = result[i].allNodesLoc2;
					var cloneLocRate = 0.00;
					var cloneNodesCoChangeTimes = result[i].cloneNodesCoChangeTimes;
					var allNodesCoChangeTimes = result[i].allNodesCoChangeTimes;
					var cloneCoChangeRate = 0.00;
					var cloneType1Count = result[i].cloneType1Count;
					var cloneType2Count = result[i].cloneType2Count;
					var cloneType3Count = result[i].cloneType3Count;
					var cloneType = "";
					var cloneSimilarityValue = result[i].cloneSimilarityValue;
					var cloneSimilarityRate = 0.00;
					if(allNodesCount1 + allNodesCount2 > 0) {
						cloneMatchRate = (cloneNodesCount1 + cloneNodesCount2 + 0.00) / (allNodesCount1 + allNodesCount2);
					}
					if(allNodesLoc1 + allNodesLoc2 > 0) {
						cloneLocRate = (cloneNodesLoc1 + cloneNodesLoc2 + 0.00) / (allNodesLoc1 + allNodesLoc2);
					}
					if(allNodesCoChangeTimes > 0) {
						cloneCoChangeRate = (cloneNodesCoChangeTimes + 0.00) / allNodesCoChangeTimes;
					}
					if(cloneType1Count + cloneType2Count + cloneType3Count > 0) {
						cloneSimilarityRate = (cloneSimilarityValue + 0.00) / (cloneType1Count + cloneType2Count + cloneType3Count);
					}
					if(cloneType3Count > 0) {
						cloneType = "type_3";
					}
					else if(cloneType2Count > 0) {
						cloneType = "type_2";
					}
					else if(cloneType1Count > 0) {
						cloneType = "type_1";
					}
					html += "<tr>";
					html += "<td>";
					html += i + 1;
					html += "</td>";
					html += "<td>";
					html += result[i].node1.directoryPath;
					html += "</td>";
					html += "<td>";
					html += result[i].node2.directoryPath;
					html += "</td>";
					html += "<td>";
					html += "(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + cloneMatchRate.toFixed(2);
					html += "</td>";
					html += "<td>";
					html += "(" + cloneNodesLoc1 + "+" + cloneNodesLoc2 + ")/(" + allNodesLoc1 + "+" + allNodesLoc2 + ")=" + cloneLocRate.toFixed(2);
					html += "</td>";
					html += "<td>";
					if(clonePairs > 0) {
						html += cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "=" + cloneCoChangeRate.toFixed(2);
					}
					html += "</td>";
					html += "<td>";
					if(clonePairs > 0) {
						html += cloneSimilarityValue.toFixed(2) + "/(" + cloneType1Count + "+" + cloneType2Count + "+" + cloneType3Count + ")=" + cloneSimilarityRate.toFixed(2);
					}
					html += "</td>";
					html += "<td>";
					if(clonePairs > 0) {
						html += cloneType;
					}
					html += "</td>";
					html += "<td>";
					if(clonePairs > 0) {
						html += "<a target='_blank' class='package' href='/cloneaggregation/details" +
							"?id1=" + result[i].node1.id +
							"&id2=" + result[i].node2.id +
							"&path1=" + result[i].node1.directoryPath +
							"&path2=" + result[i].node2.directoryPath +
							"&clonePairs=" + clonePairs +
							"&cloneNodesCount1=" + cloneNodesCount1 +
							"&cloneNodesCount2=" + cloneNodesCount2 +
							"&allNodesCount1=" + allNodesCount1 +
							"&allNodesCount2=" + allNodesCount2 +
							"&cloneMatchRate=" + cloneMatchRate +
							"&cloneNodesLoc1=" + cloneNodesLoc1 +
							"&cloneNodesLoc2=" + cloneNodesLoc2 +
							"&allNodesLoc1=" + allNodesLoc1 +
							"&allNodesLoc2=" + allNodesLoc2 +
							"&cloneLocRate=" + cloneLocRate +
							"&cloneNodesCoChangeTimes=" + cloneNodesCoChangeTimes +
							"&allNodesCoChangeTimes=" + allNodesCoChangeTimes +
							"&cloneCoChangeRate=" + cloneCoChangeRate +
							"&cloneType1Count=" + cloneType1Count +
							"&cloneType2Count=" + cloneType2Count +
							"&cloneType3Count=" + cloneType3Count +
							"&cloneType=" + cloneType +
							"&cloneSimilarityValue=" + cloneSimilarityValue +
							"&cloneSimilarityRate=" + cloneSimilarityRate +
							"'>" + clonePairs + "</a>";
					}
					else {
						html += clonePairs;
					}
					html += "</td>";
					html += "</tr>";
					html += "<tr>";
				}
				html += "</table>";
				$("#packages_clone").html(html);
				// $(".package").click(function() {
				// 	doublePackagesCloneWithCoChange($(this).attr("id1"), $(this).attr("id2"), $(this).attr("index"));
				// });
			}
		});
	}
	return {
		init : function() {
			packagesClone();
		}
	}
}
