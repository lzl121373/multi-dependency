package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.smell.MultipleSmellDetector;
import cn.edu.fudan.se.multidependency.service.query.smell.data.CirclePacking;
import cn.edu.fudan.se.multidependency.service.query.smell.data.MultipleAS;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/as/overview")
public class SmellOverviewController {
	
	@Autowired
	private MultipleSmellDetector multipleSmellDetector;
	
	@Autowired
	private NodeService nodeService;

	@GetMapping("")
	public String smellOverview(HttpServletRequest request) {
		request.setAttribute("projects", nodeService.allProjects());
		request.setAttribute("projectTotalMap", multipleSmellDetector.getProjectTotal());
		request.setAttribute("fileSmellOverviewMap", multipleSmellDetector.getFileSmellOverview());
		request.setAttribute("packageSmellOverviewMap", multipleSmellDetector.getPackageSmellOverview());
		return "as/smelloverview";
	}
	
	@GetMapping("/histogram")
	@ResponseBody
	public Object histogram() {
		return multipleSmellDetector.projectHistogramOnVersion();
	}

	@GetMapping("/pie")
	@ResponseBody
	public Object issue(
			@RequestParam(name= "cyclicDependency", required=false, defaultValue="true") boolean cyclicDependency,
			@RequestParam(name= "hubLikeDependency", required=false, defaultValue="true") boolean hubLikeDependency,
			@RequestParam(name= "unstableDependency", required=false, defaultValue="true") boolean unstableDependency,
			@RequestParam(name= "unstableInterface", required=false, defaultValue="true") boolean unstableInterface,
			@RequestParam(name= "implicitCrossModuleDependency", required=false, defaultValue="true") boolean implicitCrossModuleDependency,
			@RequestParam(name= "unutilizedAbstraction", required=false, defaultValue="true") boolean unutilizedAbstraction,
			@RequestParam(name= "unuUnusedInclude", required=false, defaultValue="true") boolean unuUnusedInclude) {
		return multipleSmellDetector.smellAndIssueFiles(new MultipleAS() {
			@Override
			public boolean isCyclicDependency() {
				return cyclicDependency;
			}
			@Override
			public boolean isHubLikeDependency() {
				return hubLikeDependency;
			}
			@Override
			public boolean isUnstableDependency() {
				return unstableDependency;
			}
			@Override
			public boolean isUnstableInterface() {
				return unstableInterface;
			}
			@Override
			public boolean isImplicitCrossModuleDependency() {
				return implicitCrossModuleDependency;
			}
			@Override
			public boolean isUnutilizedAbstraction() {
				return unutilizedAbstraction;
			}
			@Override
			public boolean isUnusedInclude() {
				return unuUnusedInclude;
			}
		});
	}
	
	@GetMapping("/overview/{projectId}")
	public String projectMultiple(HttpServletRequest request, @PathVariable("projectId") long projectId,
								  @RequestParam(name= "cyclicDependency", required=false, defaultValue="true") boolean cyclicDependency,
								  @RequestParam(name= "hubLikeDependency", required=false, defaultValue="true") boolean hubLikeDependency,
								  @RequestParam(name= "unstableDependency", required=false, defaultValue="true") boolean unstableDependency,
								  @RequestParam(name= "unstableInterface", required=false, defaultValue="true") boolean unstableInterface,
								  @RequestParam(name= "implicitCrossModuleDependency", required=false, defaultValue="true") boolean implicitCrossModuleDependency,
								  @RequestParam(name= "unutilizedAbstraction", required=false, defaultValue="true") boolean unutilizedAbstraction,
								  @RequestParam(name= "unuUnusedInclude", required=false, defaultValue="true") boolean unuUnusedInclude) {
		Project project = nodeService.queryProject(projectId);
		request.setAttribute("project", project);
		Map<Long, List<CirclePacking>> circlePackings = multipleSmellDetector.circlePacking(new MultipleAS() {
			@Override
			public boolean isCyclicDependency() {
				return cyclicDependency;
			}
			@Override
			public boolean isHubLikeDependency() {
				return hubLikeDependency;
			}
			@Override
			public boolean isUnstableDependency() {
				return unstableDependency;
			}
			@Override
			public boolean isUnstableInterface() {
				return unstableInterface;
			}
			@Override
			public boolean isImplicitCrossModuleDependency() {
				return implicitCrossModuleDependency;
			}
			@Override
			public boolean isUnutilizedAbstraction() {
				return unutilizedAbstraction;
			}
			@Override
			public boolean isUnusedInclude() {
				return unuUnusedInclude;
			}
		});
		List<CirclePacking> circlePacking = circlePackings.getOrDefault(project.getId(), new ArrayList<>());
		request.setAttribute("circlePacking", circlePacking);
		return "as/multipleproject";
	}
}
