let smellOverview = function() {
	const SMELL_TYPE = {
		CYCLIC_DEPENDENCY: "CyclicDependency",
		HUBLIKE_DEPENDENCY: "HubLikeDependency",
		UNSTABLE_DEPENDENCY: "UnstableDependency",
		UNSTABLE_INTERFACE: "UnstableInterface",
		IMPLICIT_CROSS_MODULE_DEPENDENCY: "ImplicitCrossModuleDependency",
		UNUTILIZED_ABSTRACTION: "UnutilizedAbstraction",
		UNUSED_INCLUDE: "UnusedInclude"
	};
	const SMELL_LEVEL = {
		TYPE: "Type",
		FILE: "File",
		PACKAGE: "Package"
	};
	let _smellOverview = function(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap) {
		let html = "";
		for(let projectIndex in projects) {
			if (projects.hasOwnProperty(projectIndex)) {
				let project = projects[projectIndex];
				html += "<div>";
				html += "<div>";
				html += "<h4><a target='_blank' href='/as/overview/" + project.id + paramToRequestParam() + "'>" + project.name + " (" + project.language + ") </h4></a>";
				html += "</div>";
				html += "<div  style='width: 100%'>";
				html += "<div class='col-sm-4'>";
				html += "<div id='allFilesPie_" + project.id + "' style='height: 400px;'></div>";
				html += "</div>";
				html += "<div class='col-sm-4'>";
				html += "<div id='issueFilesPie_" + project.id + "' style='height: 400px;'></div>";
				html += "</div>";
				html += "<div class='col-sm-4'>";
				html += "<div id='issuesPie_" + project.id + "' style='height: 400px;'></div>";
				html += "</div>";
				html += "</div>";
				html += "<div style='width: 100%'>";
				html += "<div id='circle_" + project.id + "'></div>";
				html += "</div>";

				let projectTotalObject = projectTotalMap[project.id];
				let fileCount = projectTotalObject["FileCount"];
				let commits = projectTotalObject["Commits"];
				let changeLines = projectTotalObject["ChangeLines"];
				let issueCommits = projectTotalObject["IssueCommits"];
				let issueChangeLines = projectTotalObject["IssueChangeLines"];

				let projectFileSmellOverviewObject = fileSmellOverviewMap[project.id];
				let projectFileSmellArray = projectFileSmellOverviewObject["ProjectFileSmell"];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>File Smell Type</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Smell Count</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Change Lines(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Change Lines(%)</th>";
				html += "</tr>";
				for(let projectFileSmellIndex in projectFileSmellArray) {
					if (projectFileSmellArray.hasOwnProperty(projectFileSmellIndex)) {
						let projectFileSmellObject = projectFileSmellArray[projectFileSmellIndex];
						let projectFileSmellFileCountPercent = " (null)";
						let projectFileSmellIssueCommitsPercent = " (null)";
						let projectFileSmellCommitsPercent = " (null)";
						let projectFileSmellIssueChangeLinesPercent = " (null)";
						let projectFileSmellChangeLinesPercent = " (null)";
						if (fileCount !== 0) {
							projectFileSmellFileCountPercent = " (" + (projectFileSmellObject["FileCount"] / fileCount).toFixed(2) + ")";
						}
						if (issueCommits !== 0) {
							projectFileSmellIssueCommitsPercent = " (" + (projectFileSmellObject["IssueCommits"] / issueCommits).toFixed(2) + ")";
						}
						if (commits !== 0) {
							projectFileSmellCommitsPercent = " (" + (projectFileSmellObject["Commits"] / commits).toFixed(2) + ")";
						}
						if (issueChangeLines !== 0) {
							projectFileSmellIssueChangeLinesPercent = " (" + (projectFileSmellObject["IssueChangeLines"] / issueChangeLines).toFixed(2) + ")";
						}
						if (changeLines !== 0) {
							projectFileSmellChangeLinesPercent = " (" + (projectFileSmellObject["ChangeLines"] / changeLines).toFixed(2) + ")";
						}
						html += "<tr>";
						let smellType = projectFileSmellObject["SmellType"];
						let href = "/as";
						if (smellType === SMELL_TYPE.CYCLIC_DEPENDENCY) {
							href += "/cyclicdependency";
						}
						else if (smellType === SMELL_TYPE.HUBLIKE_DEPENDENCY) {
							href += "/hublikedependency";
						}
						else if (smellType === SMELL_TYPE.UNSTABLE_DEPENDENCY) {
							href += "/unstabledependency";
						}
						else if (smellType === SMELL_TYPE.UNSTABLE_INTERFACE) {
							href += "/unstableinterface";
						}
						else if (smellType === SMELL_TYPE.IMPLICIT_CROSS_MODULE_DEPENDENCY) {
							href += "/implicitcrossmoduledependency";
						}
						else if (smellType === SMELL_TYPE.UNUTILIZED_ABSTRACTION) {
							href += "/unutilizedabstraction";
						}
						else if (smellType === SMELL_TYPE.UNUSED_INCLUDE) {
							href += "/unusedinclude";
						}
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='" + href + "/query?projectid=" + project.id + "&smelllevel=" + SMELL_LEVEL.FILE + "'>" + smellType + "</a>" +
							"</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["SmellCount"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["FileCount"] + projectFileSmellFileCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["Commits"] + projectFileSmellCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["ChangeLines"] + projectFileSmellChangeLinesPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["IssueCommits"] + projectFileSmellIssueCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectFileSmellObject["IssueChangeLines"] + projectFileSmellIssueChangeLinesPercent + "</td>";
						html += "</tr>";
					}
				}
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>Project Total</td>";
				html += "<td style='text-align: center; vertical-align: middle'></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + fileCount + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + commits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + changeLines + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueCommits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueChangeLines + "</td>";
				html += "</tr>";
				html += "</table>";
				html += "</div>";

				let projectPackageSmellOverviewObject = packageSmellOverviewMap[project.id];
				let projectPackageSmellArray = projectPackageSmellOverviewObject["ProjectPackageSmell"];
				html += "<div>";
				html += "<table class='table table-bordered'>";
				html += "<tr>";
				html += "<th style='text-align: center; vertical-align: middle'>Package Smell Type</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Package Smell Count</th>";
				html += "<th style='text-align: center; vertical-align: middle'>File Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Change Lines(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Commit Count(%)</th>";
				html += "<th style='text-align: center; vertical-align: middle'>Issue Change Lines(%)</th>";
				html += "</tr>";
				for(let projectPackageSmellIndex in projectPackageSmellArray) {
					if (projectPackageSmellArray.hasOwnProperty(projectPackageSmellIndex)) {
						let projectPackageSmellObject = projectPackageSmellArray[projectPackageSmellIndex];
						let projectPackageSmellFileCountPercent = " (null)";
						let projectPackageSmellIssueCommitsPercent = " (null)";
						let projectPackageSmellCommitsPercent = " (null)";
						let projectPackageSmellIssueChangeLinesPercent = " (null)";
						let projectPackageSmellChangeLinesPercent = " (null)";
						if (fileCount !== 0) {
							projectPackageSmellFileCountPercent = " (" + (projectPackageSmellObject["FileCount"] / fileCount).toFixed(2) + ")";
						}
						if (issueCommits !== 0) {
							projectPackageSmellIssueCommitsPercent = " (" + (projectPackageSmellObject["IssueCommits"] / issueCommits).toFixed(2) + ")";
						}
						if (commits !== 0) {
							projectPackageSmellCommitsPercent = " (" + (projectPackageSmellObject["Commits"] / commits).toFixed(2) + ")";
						}
						if (issueChangeLines !== 0) {
							projectPackageSmellIssueChangeLinesPercent = " (" + (projectPackageSmellObject["IssueChangeLines"] / issueChangeLines).toFixed(2) + ")";
						}
						if (changeLines !== 0) {
							projectPackageSmellChangeLinesPercent = " (" + (projectPackageSmellObject["ChangeLines"] / changeLines).toFixed(2) + ")";
						}
						html += "<tr>";
						let smellType = projectPackageSmellObject["SmellType"];
						let href = "/as";
						if (smellType === SMELL_TYPE.CYCLIC_DEPENDENCY) {
							href += "/cyclicdependency";
						}
						else if (smellType === SMELL_TYPE.HUBLIKE_DEPENDENCY) {
							href += "/hublikedependency";
						}
						else if (smellType === SMELL_TYPE.UNSTABLE_DEPENDENCY) {
							href += "/unstabledependency";
						}
						else if (smellType === SMELL_TYPE.IMPLICIT_CROSS_MODULE_DEPENDENCY) {
							href += "/implicitcrossmoduledependency";
						}
						else if (smellType === SMELL_TYPE.UNUTILIZED_ABSTRACTION) {
							href += "/unutilizedabstraction";
						}
						else if (smellType === SMELL_TYPE.UNUSED_INCLUDE) {
							href += "/unusedinclude";
						}
						html += "<td style='text-align: center; vertical-align: middle'>" +
							"<a target='_blank' href='" + href + "/query?projectid=" + project.id + "&smelllevel=" + SMELL_LEVEL.PACKAGE + "'>" + smellType + "</a>" +
							"</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["SmellCount"] + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["FileCount"] + projectPackageSmellFileCountPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["Commits"] + projectPackageSmellCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["ChangeLines"] + projectPackageSmellChangeLinesPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["IssueCommits"] + projectPackageSmellIssueCommitsPercent + "</td>";
						html += "<td style='text-align: center; vertical-align: middle'>" + projectPackageSmellObject["IssueChangeLines"] + projectPackageSmellIssueChangeLinesPercent + "</td>";
						html += "</tr>";
					}
				}
				html += "<tr>";
				html += "<td style='text-align: center; vertical-align: middle'>Project Total</td>";
				html += "<td style='text-align: center; vertical-align: middle'></td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + fileCount + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + commits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + changeLines + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueCommits + "</td>";
				html += "<td style='text-align: center; vertical-align: middle'>" + issueChangeLines + "</td>";
				html += "</tr>";
				html += "</table>";
				html += "</div>";
				html += "</div>";
			}
			$.ajax({
				type: "get",
				url: "/as/overview/histogram",
				success: function(result) {
					let allFiles = [];
					let smellFiles = [];
					let issueFiles = [];
					let projectsName = [];
					for (let i = 0; i < projects.length; i++) {
						let project = projects[i];
						allFiles[i] = result[project.id].allFilesCount;
						smellFiles[i] = result[project.id].smellFilesCount;
						issueFiles[i] = result[project.id].issueFilesCount;
						projectsName[i] = project.name;
					}
					let data = {
						allFiles: allFiles,
						smellFiles: smellFiles,
						issueFiles: issueFiles,
						projects: projectsName
					};
					_histogram(data, "histogram");
				}
			});

			$.ajax({
				type: "get",
				url: "/as/overview/pie",
				data : param,
				success: function(result) {
					for(let i = 0; i < projects.length; i++) {
						let project = projects[i];
						_pie(project, result[project.id], "allFilesPie_" + project.id, "issueFilesPie_" + project.id, "issuesPie_" + project.id);
					}
				}
			});
		}
		$("#content").html(html);
	};

	let _histogram = function(data, divId) {
		let multipleHistogram = echarts.init(document.getElementById(divId));
		let option = {
			dataZoom: [{
				type: 'slider',
				show: true,
				xAxisIndex: [0],
				left: '2%',
				bottom: 0,
				start: 0,
				end: 100
			}],
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'shadow'
				}
			},
			legend: {
				data: ["All Files", 'Smell Files', 'Issue Files']
			},
			grid: {
				left: '0%',
				right: '0%',
				bottom: '0%',
				containLabel: true
			},
			xAxis: [{
				type: 'category',
				data: data.projects,
				axisLabel: {
					interval:0,
					rotate:40
				}
			}],
			yAxis: [{
				type: 'value'
			}],
			series: [{
				name: "All Files",
				type: 'bar',
				stack: 'allFiles',
				data: data.allFiles
			},{
				name: 'Smell Files',
				type: 'bar',
				stack: 'smellFiles',
				data: data.smellFiles
			},{
				name: 'Issue Files',
				type: 'bar',
				stack: 'issueFiles',
				data: data.issueFiles
			}]
		};
		multipleHistogram.setOption(option);
	};

	let _pie = function(project, pies, allFilesPieDivId, smellAndIssueFilesPieDivId, issuesDivId) {
		let allFilesPie = echarts.init(document.getElementById(allFilesPieDivId));
		let smellAndIssueFilesPie = echarts.init(document.getElementById(smellAndIssueFilesPieDivId));
		let issuesPie = echarts.init(document.getElementById(issuesDivId));

		let totalfile1 = pies.normalFiles.length + pies.onlyIssueFiles.length + pies.issueAndSmellFiles.length + pies.onlySmellFiles.length;
		let totalfile2 = pies.onlyIssueFiles.length + pies.issueAndSmellFiles.length + pies.onlySmellFiles.length;

		let allFilesOption = {
			title: {
				text: '文件占比',
				left: 'center'
			},
			tooltip: {
				trigger: 'item',
				formatter: '{a} <br/>{b}'
			},
			legend: {
				orient: 'vertical',
				left: 'left',
				data: ['normalFiles', 'onlyIssueFiles', 'issueAndSmellFiles', 'onlySmellFiles']
			},
			series: [
				{
					name: '文件',
					type: 'pie',
					radius: '55%',
					center: ['50%', '60%'],
					data: [
						{value: pies.normalFiles.length
							, name: 'normalFiles : ' + pies.normalFiles.length + " (" + ((pies.normalFiles.length / totalfile1) * 100).toFixed(2) + '%)'},
						{value: pies.onlyIssueFiles.length
							, name: 'onlyIssueFiles : ' + pies.onlyIssueFiles.length + " (" + ((pies.onlyIssueFiles.length / totalfile1) * 100).toFixed(2) + '%)'},
						{value: pies.issueAndSmellFiles.length
							, name: 'issueAndSmellFiles : ' + pies.issueAndSmellFiles.length + " (" + ((pies.issueAndSmellFiles.length / totalfile1) * 100).toFixed(2) + '%)'},
						{value: pies.onlySmellFiles.length
							, name: 'onlySmellFiles : ' + pies.onlySmellFiles.length + " (" + ((pies.onlySmellFiles.length / totalfile1) * 100).toFixed(2) + '%)'}
					],
					emphasis: {
						itemStyle: {
							shadowBlur: 10,
							shadowOffsetX: 0,
							shadowColor: 'rgba(0, 0, 0, 0.5)'
						}
					}
				}
			]
		};
		allFilesPie.setOption(allFilesOption);

		let warnFilesOption = {
			title: {
				text: '文件占比',
				left: 'center'
			},
			tooltip: {
				trigger: 'item',
				formatter: '{a} <br/>{b}'
			},
			legend: {
				orient: 'vertical',
				left: 'left',
				data: ['onlyIssueFiles', 'issueAndSmellFiles', 'onlySmellFiles']
			},
			series: [
				{
					name: '文件',
					type: 'pie',
					radius: '55%',
					center: ['50%', '60%'],
					data: [
						{value: pies.onlyIssueFiles.length
							, name: 'onlyIssueFiles : ' + pies.onlyIssueFiles.length + " (" + ((pies.onlyIssueFiles.length / totalfile2) * 100).toFixed(2) + '%)'},
						{value: pies.issueAndSmellFiles.length
							, name: 'issueAndSmellFiles : ' + pies.issueAndSmellFiles.length + " (" + ((pies.issueAndSmellFiles.length / totalfile2) * 100).toFixed(2) + '%)'},
						{value: pies.onlySmellFiles.length
							, name: 'onlySmellFiles : ' + pies.onlySmellFiles.length + " (" + ((pies.onlySmellFiles.length / totalfile2) * 100).toFixed(2) + '%)'}
					],
					emphasis: {
						itemStyle: {
							shadowBlur: 10,
							shadowOffsetX: 0,
							shadowColor: 'rgba(0, 0, 0, 0.5)'
						}
					}
				}
			]
		};
		smellAndIssueFilesPie.setOption(warnFilesOption);

		let issuesPieOption = {
			title: {
				text: 'Issues占比',
				left: 'center'
			},
			tooltip: {
				trigger: 'item',
				formatter: '{a} <br/>{b}'
			},
			legend: {
				orient: 'vertical',
				left: 'left',
				data: ['无Smell Files关联的Issues', '有Smell File关联的Issues']
			},
			series: [
				{
					name: '文件',
					type: 'pie',
					radius: '55%',
					center: ['50%', '60%'],
					data: [
						{value: (pies.allIssues.length - pies.smellIssues.length)
							, name: '无Smell Files关联的Issues : ' + (pies.allIssues.length - pies.smellIssues.length) + " (" + (((pies.allIssues.length - pies.smellIssues.length) / pies.allIssues.length) * 100).toFixed(2) + '%)'},
						{value: pies.smellIssues.length
							, name: '有Smell File关联的Issues : ' + pies.smellIssues.length + " (" + ((pies.smellIssues.length / pies.allIssues.length) * 100).toFixed(2) + '%)'}
					],
					emphasis: {
						itemStyle: {
							shadowBlur: 10,
							shadowOffsetX: 0,
							shadowColor: 'rgba(0, 0, 0, 0.5)'
						}
					}
				}
			]
		};
		issuesPie.setOption(issuesPieOption);
	};

	let param = {
		cyclicDependency: true,
		hubLikeDependency: true,
		unstableDependency: true,
		unstableInterface: true,
		implicitCrossModuleDependency: true,
		unutilizedAbstraction: true,
		unuUnusedInclude: true
	};

	let paramToRequestParam = function () {
		return "?cyclicDependency=" + param.cyclicDependency +
			"&hubLikeDependency=" + param.hubLikeDependency +
			"&unstableDependency=" + param.unstableDependency +
			"&unstableInterface=" + param.unstableInterface +
			"&implicitCrossModuleDependency=" + param.implicitCrossModuleDependency +
			"&unutilizedAbstraction=" + param.unutilizedAbstraction +
			"&unuUnusedInclude=" + param.unuUnusedInclude;
	};

	return {
		smellOverview: function(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap) {
			_smellOverview(projects, projectTotalMap, fileSmellOverviewMap, packageSmellOverviewMap);
		}
	};
};
