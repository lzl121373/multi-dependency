package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.code.Namespace;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/relation/namespace/{namespaceId}")
public class NamespaceRelationController {
    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContainRelationService containRelationService;

    @GetMapping("/contain/type")
    @ResponseBody
    public Object containType(@PathVariable("namespaceId") long id) {
        Namespace namespace = nodeService.queryNamespace(id);
        return containRelationService.findNamespaceDirectlyContainTypes(namespace);
    }

    @GetMapping("/contain/variable")
    @ResponseBody
    public Object containVariable(@PathVariable("namespaceId") long id) {
        Namespace namespace = nodeService.queryNamespace(id);
        return containRelationService.findNamespaceDirectlyContainVariables(namespace);
    }

    @GetMapping("/contain/function")
    @ResponseBody
    public Object containFunction(@PathVariable("fileId") long id) {
        Namespace namespace = nodeService.queryNamespace(id);
        return containRelationService.findNamespaceDirectlyContainFunctions(namespace);
    }
}
