let smellDetect = function() {
	const SMELL_LEVEL = {
		MULTIPLE_LEVEL: "MultipleLevel"
	};
	let _set = function() {
		let setHubLikeDependencyThreshold = function(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut) {
			$.ajax({
				type: "post",
				url: "/as/hublikedependency/fanio/" + projectId
					+ "?minFileFanIn=" + minFileFanIn
					+ "&minFileFanOut=" + minFileFanOut
					+ "&minPackageFanIn=" + minPackageFanIn
					+ "&minPackageFanOut=" + minPackageFanOut,
				success: function(result) {
					if (result === true) {
						alert("修改成功！");
					}
					else {
						alert("修改失败！");
					}
				}
			});
		};
		$("#hubLikeDependencyThresholdSave").click(function() {
			let projectId = $("#hubLikeDependencyProject").val();
			let minFileFanIn = $("#hubLikeMinFileFanIn").val();
			let minFileFanOut = $("#hubLikeMinFileFanOut").val();
			let minPackageFanIn = $("#hubLikeMinPackageFanIn").val();
			let minPackageFanOut = $("#hubLikeMinPackageFanOut").val();
			setHubLikeDependencyThreshold(projectId, minFileFanIn, minFileFanOut, minPackageFanIn, minPackageFanOut);
		});

		let setUnstableDependencyThreshold = function(projectId,  minFileFanOut, coChangeTimes, minRatio) {
			$.ajax({
				type: "post",
				url: "/as/unstabledependency/threshold/history/" + projectId
					+ "?minFileFanOut=" + minFileFanOut
					+ "&coChangeTimes=" + coChangeTimes
					+ "&minRatio=" + minRatio,
				success: function(result) {
					if (result === true) {
						alert("修改成功！");
					}
					else {
						alert("修改失败！");
					}
				}
			});
		};
		$("#unstableDependencyThresholdSave").click(function() {
			let projectId = $("#unstableDependencyProject").val();
			let minFileFanOut = $("#unstableDependencyMinFileFanOut").val();
			let coChangeTimes = $("#unstableDependencyCoChangeTimes").val();
			let minRatio = $("#unstableDependencyMinRatio").val();
			setUnstableDependencyThreshold(projectId, minFileFanOut, coChangeTimes, minRatio);
		});

		let setUnstableInterfaceThreshold = function(projectId, minFileFanIn, coChangeTimes, minRatio) {
			$.ajax({
				type: "post",
				url: "/as/unstableInterface/threshold/" + projectId
					+ "?minFileFanIn=" + minFileFanIn
					+ "&coChangeTimes=" + coChangeTimes
					+ "&minRatio=" + minRatio,
				success: function(result) {
					if (result === true) {
						alert("修改成功！");
					}
					else {
						alert("修改失败！");
					}
				}
			});
		};
		$("#unstableInterfaceThresholdSave").click(function() {
			let projectId = $("#unstableInterfaceProject").val();
			let minFileFanIn = $("#unstableInterfaceMinFileFanIn").val();
			let coChangeTimes = $("#unstableInterfaceCoChangeTimes").val();
			let minRatio = $("#unstableInterfaceMinRatio").val();
			setUnstableInterfaceThreshold(projectId, minFileFanIn, coChangeTimes, minRatio);
		});

		let setImplicitCrossModuleDependencyThreshold = function(projectId, minFileCoChange, minPackageCoChange) {
			$.ajax({
				type: "post",
				url: "/as/implicitcrossmoduledependency/cochange/" + projectId
					+ "?minFileCoChange=" + minFileCoChange
					+ "&minPackageCoChange=" + minPackageCoChange,
				success: function(result) {
					if(result === true) {
						alert("修改成功！");
					} else {
						alert("修改失败！");
					}
				}
			});
		};
		$("#implicitCrossModuleDependencyThresholdSave").click(function() {
			let projectId = $("#implicitCrossModuleDependencyProject").val();
			let minFileCoChange = $("#icdMinFileCoChange").val();
			let minPackageCoChange = $("#icdMinPackageCoChange").val();
			setImplicitCrossModuleDependencyThreshold(projectId, minFileCoChange, minPackageCoChange);
		});
	};
	
	let _get = function() {
		let getHubLikeDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/hublikedependency/fanio/" + projectId,
				success: function(result) {
					$("#hubLikeMinFileFanIn").val(result[0]);
					$("#hubLikeMinFileFanOut").val(result[1]);
					$("#hubLikeMinPackageFanIn").val(result[2]);
					$("#hubLikeMinPackageFanOut").val(result[3]);
				}
			});
		};
		let hubLikeDependencyProject = $("#hubLikeDependencyProject");
		hubLikeDependencyProject.change(function() {
			getHubLikeDependencyThreshold($(this).val())
		});
		if (hubLikeDependencyProject.val() != null) {
			getHubLikeDependencyThreshold(hubLikeDependencyProject.val());
		}

		let getUnstableDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstabledependency/threshold/history/" + projectId,
				success: function(result) {
					$("#unstableDependencyMinFileFanOut").val(result[0]);
					$("#unstableDependencyCoChangeTimes").val(result[1]);
					$("#unstableDependencyMinRatio").val(result[2]);
				}
			});
		};
		let unstableDependencyProject = $("#unstableDependencyProject");
		unstableDependencyProject.change(function() {
			getUnstableDependencyThreshold($(this).val());
		});
		if (unstableDependencyProject.val() != null) {
			getUnstableDependencyThreshold(unstableDependencyProject.val());
		}

		let getUnstableInterfaceThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/unstableinterface/threshold/" + projectId,
				success: function(result) {
					$("#unstableInterfaceMinFileFanIn").val(result[0]);
					$("#unstableInterfaceCoChangeTimes").val(result[1]);
					$("#unstableInterfaceMinRatio").val(result[2]);
				}
			});
		};
		let unstableInterfaceProject = $("#unstableInterfaceProject");
		unstableInterfaceProject.change(function() {
			getUnstableInterfaceThreshold($(this).val());
		});
		if (unstableInterfaceProject.val() != null) {
			getUnstableInterfaceThreshold(unstableInterfaceProject.val());
		}

		let getImplicitCrossModuleDependencyThreshold = function(projectId) {
			$.ajax({
				type: "get",
				url: "/as/implicitcrossmoduledependency/cochange/" + projectId,
				success: function(result) {
					$("#icdMinFileCoChange").val(result[0]);
					$("#icdMinPackageCoChange").val(result[1]);
				}
			});
		};
		let implicitCrossModuleDependencyProject = $("#implicitCrossModuleDependencyProject");
		implicitCrossModuleDependencyProject.change(function() {
			getImplicitCrossModuleDependencyThreshold($(this).val());
		});
		if(implicitCrossModuleDependencyProject.val() != null) {
			getImplicitCrossModuleDependencyThreshold(implicitCrossModuleDependencyProject.val());
		}
	};

	let _smellQuery = function () {
		// Cyclic Dependency
		$("#cyclicDependencyQuery").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/cyclicdependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Hub-Like Dependency
		$("#hubLikeDependencyQuery").click(function() {
			let projectId = $("#hubLikeDependencyProject").val();
			window.open("/as/hublikedependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unstable Dependency
		$("#unstableDependencyQuery").click(function() {
			let projectId = $("#unstableDependencyProject").val();
			window.open("/as/unstabledependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unstable Interface
		$("#unstableInterfaceQuery").click(function() {
			let projectId = $("#unstableInterfaceProject").val();
			window.open("/as/unstableinterface/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Implicit Cross Module Dependency
		$("#implicitCrossModuleDependencyQuery").click(function() {
			let projectId = $("#implicitCrossModuleDependencyProject").val();
			window.open("/as/implicitcrossmoduledependency/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unutilized Abstraction
		$("#unutilizedAbstractionQuery").click(function() {
			let projectId = $("#unutilizedAbstractionProject").val();
			window.open("/as/unutilizedabstraction/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unused Include
		$("#unusedIncludeQuery").click(function() {
			let projectId = $("#unusedIncludeProject").val();
			window.open("/as/unusedinclude/query?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Multiple Smell
		$("#multipleSmellQuery").click(function() {
			let projectId = $("#multipleSmellProject").val();
			window.open("/as/multiplesmell/query?projectid=" + projectId);
		});
	};

	let _smellDetect = function () {
		// Cyclic Dependency
		$("#cyclicDependencyDetect").click(function() {
			let projectId = $("#cyclicDependencyProject").val();
			window.open("/as/cyclicdependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Hub-Like Dependency
		$("#hubLikeDependencyDetect").click(function() {
			let projectId = $("#hubLikeDependencyProject").val();
			window.open("/as/hublikedependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unstable Dependency
		$("#unstableDependencyDetect").click(function() {
			let projectId = $("#unstableDependencyProject").val();
			window.open("/as/unstabledependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unstable Interface
		$("#unstableInterfaceDetect").click(function() {
			let projectId = $("#unstableInterfaceProject").val();
			window.open("/as/unstableinterface/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Implicit Cross Module Dependency
		$("#implicitCrossModuleDependencyDetect").click(function() {
			let projectId = $("#implicitCrossModuleDependencyProject").val();
			window.open("/as/implicitcrossmoduledependency/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unutilized Abstraction
		$("#unutilizedAbstractionDetect").click(function() {
			let projectId = $("#unutilizedAbstractionProject").val();
			window.open("/as/unutilizedabstraction/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Unused Include
		$("#unusedIncludeDetect").click(function() {
			let projectId = $("#unusedIncludeProject").val();
			window.open("/as/unusedinclude/detect?projectid=" + projectId + "&smelllevel=" + SMELL_LEVEL.MULTIPLE_LEVEL);
		});
		// Multiple Smell
		$("#multipleSmellDetect").click(function() {
			let projectId = $("#multipleSmellProject").val();
			window.open("/as/multiplesmell/detect?projectid=" + projectId);
		});
	};

	let _smellExport = function () {
		// Unused Include
		let exportUnusedInclude = function(projectId) {
			$.ajax({
				type: "post",
				url: "/as/unusedinclude/export?projectid=" + projectId,
				success: function(result) {
					if(result === true) {
						alert("导出成功！");
					} else {
						alert("导出失败！");
					}
				}
			});
		};
		$("#unusedIncludeExport").click(function() {
			let projectId = $("#unusedIncludeProject").val();
			exportUnusedInclude(projectId);
		});
	};
	
	return {
		init : function() {
			_set();
			_get();
		},
		smellQuery : function () {
			_smellQuery();
		},
		smellDetect : function () {
			_smellDetect();
		},
		smellExport : function () {
			_smellExport();
		}
	};
};
