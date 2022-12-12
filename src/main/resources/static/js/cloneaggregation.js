var showAggregationResult = function(language, id) {
	$("#aggregation_java").html("");
	$.ajax({
		type: "get",
		url: "/cloneaggregation/show/" + language + "?threshold=10&percentage=0.8",
		timeout: 0,
		success: function(result) {
			console.log("success");
			var html = "<table class='table table-bordered'>";
			html += "<tr><th>目录1</th><th>目录2</th><th>克隆文件占比</th><th>克隆Loc占比</th><th>克隆CoChange占比</th><th>克隆相似度</th><th>type</th><th>克隆对数</th></tr>";
			var tr = function(index, layer, duplicated) {
				var prefix = "";
				for(var i = 0; i < layer; i++) {
					prefix += "|---";
				}
				switch (index) {
					case 0:
						var clonePairs = duplicated.packagePairRelationData.clonePairs;
						var cloneNodesCount1 = duplicated.packagePairRelationData.cloneNodesCount1;
						var cloneNodesCount2 = duplicated.packagePairRelationData.cloneNodesCount2;
						var allNodesCount1 = duplicated.packagePairRelationData.allNodesCount1;
						var allNodesCount2 = duplicated.packagePairRelationData.allNodesCount2;
						var cloneMatchRate = duplicated.packagePairRelationData.cloneMatchRate;
						var cloneNodesLoc1 = duplicated.packagePairRelationData.cloneNodesLoc1;
						var cloneNodesLoc2 = duplicated.packagePairRelationData.cloneNodesLoc2;
						var allNodesLoc1 = duplicated.packagePairRelationData.allNodesLoc1;
						var allNodesLoc2 = duplicated.packagePairRelationData.allNodesLoc2;
						var cloneLocRate = duplicated.packagePairRelationData.cloneLocRate;
						var cloneNodesCoChangeTimes = duplicated.packagePairRelationData.cloneNodesCoChangeTimes;
						var allNodesCoChangeTimes = duplicated.packagePairRelationData.allNodesCoChangeTimes;
						var cloneCoChangeRate = duplicated.packagePairRelationData.cloneCoChangeRate;
						var cloneType1Count = duplicated.packagePairRelationData.cloneType1Count;
						var cloneType2Count = duplicated.packagePairRelationData.cloneType2Count;
						var cloneType3Count = duplicated.packagePairRelationData.cloneType3Count;
						var cloneType = duplicated.packagePairRelationData.cloneType;
						var cloneSimilarityValue = duplicated.packagePairRelationData.cloneSimilarityValue;
						var cloneSimilarityRate = duplicated.packagePairRelationData.cloneSimilarityRate;
						html += "<tr>";
						html += layer === 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package1.directoryPath;
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						html += prefix + duplicated.package2.directoryPath;
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						html += "(" + cloneNodesCount1 + "+" + cloneNodesCount2 + ")/(" + allNodesCount1 + "+" + allNodesCount2 + ")=" + cloneMatchRate.toFixed(2);
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						html += "(" + cloneNodesLoc1 + "+" + cloneNodesLoc2 + ")/(" + allNodesLoc1 + "+" + allNodesLoc2 + ")=" + cloneLocRate.toFixed(2);
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += cloneNodesCoChangeTimes  + "/" + allNodesCoChangeTimes  + "=" + cloneCoChangeRate.toFixed(2);
						}
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += cloneSimilarityValue.toFixed(2) + "/(" + cloneType1Count + "+" + cloneType2Count + "+" + cloneType3Count + ")=" + cloneSimilarityRate.toFixed(2);
						}
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += cloneType;
						}
						html += layer === 0 ? "</th>" : "</td>";
						html += layer === 0 ? "<th>" : "<td>";
						if(clonePairs > 0) {
							html += "<a target='_blank' class='package' href='/cloneaggregation/details" +
								"?id1=" + duplicated.package1.id +
								"&id2=" + duplicated.package2.id +
								"&path1=" + duplicated.package1.directoryPath +
								"&path2=" + duplicated.package2.directoryPath +
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
						html += layer === 0 ? "</th>" : "</td>";
						html += "</tr>";
						break;
					case -1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
					case 1:
						html += "<tr style='color: #A9A9A9'>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += prefix + duplicated.directoryPath;
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "<td>";
						html += "</td>";
						html += "</tr>";
						break;
				}
				if(index === 0) {
					for(var key1 = 0; key1 < duplicated.childrenHotspotPackagePairs.length; key1 ++) {
						tr(0, layer + 1, duplicated.childrenHotspotPackagePairs[key1]);
					}

					for(var key2 = 0; key2 < duplicated.childrenOtherPackages1.length; key2 ++) {
						tr(-1, layer + 1, duplicated.childrenOtherPackages1[key2]);
					}

					for(var key3 = 0; key3 < duplicated.childrenOtherPackages2.length; key3 ++) {
						tr(1, layer + 1, duplicated.childrenOtherPackages2[key3]);
					}
				}
			}
			for(var i = 0; i < result.length; i ++) {
				tr(0, 0, result[i]);
			}
			html += "</table>"
			$(id).html(html);
		},
		error: function () {
			alert("502!");
		}
	})
}