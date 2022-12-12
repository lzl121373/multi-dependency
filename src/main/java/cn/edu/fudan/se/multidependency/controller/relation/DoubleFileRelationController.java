package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.relation.DependsOn;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.Serializable;
import java.util.Collection;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/relation/file/double/{file1Id}/{file2Id}")
public class DoubleFileRelationController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private StaticAnalyseService staticAnalyseService;

    @GetMapping("/commonDependsOn")
    @ResponseBody
    public Object commonDependsOn(@PathVariable("file1Id") long id1, @PathVariable("file2Id") long id2){
        ProjectFile file1 = nodeService.queryFile(id1);
        ProjectFile file2 = nodeService.queryFile(id2);
        return staticAnalyseService.findFilesCommonDependsOn(file1,file2);
    }

    @GetMapping("/commonDependedOnBy")
    @ResponseBody
    public Object commonDependedOnBy(@PathVariable("file1Id") long id1, @PathVariable("file2Id") long id2){
        ProjectFile file1 = nodeService.queryFile(id1);
        ProjectFile file2 = nodeService.queryFile(id2);
        return staticAnalyseService.findFilesCommonDependedOnBy(file1,file2);
    }

    @GetMapping("/file1DependsOn")
    @ResponseBody
    public Object file1DependsOn(@PathVariable("file1Id") long id1, @PathVariable("file2Id") long id2){
        ProjectFile file1 = nodeService.queryFile(id1);
        ProjectFile file2 = nodeService.queryFile(id2);
        Collection <ProjectFile> commonDepends = staticAnalyseService.findFilesCommonDependsOn(file1,file2);
        Collection <Node> file1Depends = (staticAnalyseService.findFileDependsOn(file1)).stream().map(DependsOn::getEndNode).collect(Collectors.toList());
        for (ProjectFile file:
             commonDepends) {
            file1Depends.remove((Node)file);
        }
        return file1Depends;
    }

    @GetMapping("/file1DependedOnBy")
    @ResponseBody
    public Object file1DependedOnBy(@PathVariable("file1Id") long id1, @PathVariable("file2Id") long id2) {
        ProjectFile file1 = nodeService.queryFile(id1);
        ProjectFile file2 = nodeService.queryFile(id2);
        Collection<ProjectFile> commonDepends = staticAnalyseService.findFilesCommonDependedOnBy(file1, file2);
        Collection<Node> file1Depends = (staticAnalyseService.findFileDependedOnBy(file1)).stream().map(DependsOn::getStartNode).collect(Collectors.toList());
        for (ProjectFile file:
                commonDepends) {
            file1Depends.remove((Node)file);
        }
        return file1Depends;
    }

}
