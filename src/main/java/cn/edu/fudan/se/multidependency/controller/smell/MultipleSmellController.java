package cn.edu.fudan.se.multidependency.controller.smell;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.repository.node.ProjectRepository;
import cn.edu.fudan.se.multidependency.service.query.smell.MultipleSmellDetector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/as/multiplesmell")
public class MultipleSmellController {
    @Autowired
    private MultipleSmellDetector multipleSmellDetector;

    @Autowired
    private ProjectRepository projectRepository;

    @GetMapping("/query")
    public String queryMultipleSmell(HttpServletRequest request, @RequestParam("projectid") Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project != null) {
            request.setAttribute("project", project);
            request.setAttribute("multipleSmellASFileMap", multipleSmellDetector.queryMultipleSmellASFile(true));
            request.setAttribute("multipleSmellASPackageMap", multipleSmellDetector.queryMultipleSmellASPackage(true));
        }
        return "as/multiplesmell";
    }

    @GetMapping("/detect")
    public String detectMultipleSmell(HttpServletRequest request, @RequestParam("projectid") Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project != null) {
            request.setAttribute("project", project);
            request.setAttribute("multipleSmellASFileMap", multipleSmellDetector.detectMultipleSmellASFile(true));
            request.setAttribute("multipleSmellASPackageMap", multipleSmellDetector.detectMultipleSmellASPackage(true));
        }
        return "as/multiplesmell";
    }
}
