package cn.edu.fudan.se.multidependency.controller.relation;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.relation.git.CommitUpdateFileRepository;
import cn.edu.fudan.se.multidependency.repository.relation.git.DeveloperSubmitCommitRepository;
import cn.edu.fudan.se.multidependency.service.query.StaticAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricCalculatorService;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricShowService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping("/relation/package/{packageId}")
public class PackageRelationController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContainRelationService containRelationService;

    @Autowired
    private MetricShowService metricShowService;

    @Autowired
    private StaticAnalyseService staticAnalyseService;

    @Autowired
    private GitAnalyseService gitAnalyseService;

    @Autowired
    private CommitUpdateFileRepository commitUpdateFileRepository;

    @Autowired
    private DeveloperSubmitCommitRepository developerSubmitCommitRepository;

    @GetMapping("")
    public String index(HttpServletRequest request, @PathVariable("packageId") long id){
        Package pck = nodeService.queryPackage(id);
        request.setAttribute("pck",pck);
        request.setAttribute("project",containRelationService.findPackageBelongToProject(pck));
        return"relation/package";
    }

    @GetMapping("/metric")
    @ResponseBody
    public Object metric(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return metricShowService.getPackageMetric(pck);
    }

    @GetMapping("/contain/file")
    @ResponseBody
    public Object contain(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return containRelationService.findPackageContainFiles(pck);
    }

    @GetMapping("/dependsOn")
    @ResponseBody
    public Object dependsOn(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return staticAnalyseService.findPackageDependsOn(pck);
    }

    @GetMapping("/dependedBy")
    @ResponseBody
    public Object dependedOn(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        return staticAnalyseService.findPackageDependedOnBy(pck);
    }

//    @GetMapping("/developers")
//    @ResponseBody
//    public Object developers(@PathVariable("packageId") long id) {
//        Map<Package, Map<Developer, Integer>> pckAndDev = gitAnalyseService.calCntofPckBeUpdByDev();
//        Package pck = nodeService.queryPackage(id);
//        if(pckAndDev.containsKey(pck)){
//            Map<Developer, Integer> devTimes = pckAndDev.get(pck);
//            HashMap<Developer, Integer> sortedDevTimes = new LinkedHashMap<>();
//            devTimes.entrySet()
//                    .stream()
//                    .sorted((o1, o2) -> o1.getValue().compareTo(o2.getValue()))
//                    .collect(Collectors.toList()).forEach(ele -> sortedDevTimes.put(ele.getKey(), ele.getValue()));
//            return sortedDevTimes;
//        }
//        return false;
//    }
    @GetMapping("/developerstimes")
    @ResponseBody
    public Collection<DeveloperUpdateNode<Package>> developersUpdateTimes(@PathVariable("packageId") long id) {
        Package pck = nodeService.queryPackage(id);
        Set<Commit> commits = commitUpdateFileRepository.findCommitInPackageByPackageId(id);
        Map<Developer, DeveloperUpdateNode<Package>> developerUpdateNodes = new HashMap<>();
        for(Commit commit : commits){
            Developer developer = developerSubmitCommitRepository.findDeveloperByCommitId(commit.getId());
            developerUpdateNodes.put(developer,
                    developerUpdateNodes.getOrDefault(developer,
                            new DeveloperUpdateNode<Package>(developer, pck, 0)));
            developerUpdateNodes.get(developer).addTimes(1);
        }
        return developerUpdateNodes.values();
    }
}
