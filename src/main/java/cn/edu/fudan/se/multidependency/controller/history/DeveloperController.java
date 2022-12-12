package cn.edu.fudan.se.multidependency.controller.history;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperUpdateNode;
import cn.edu.fudan.se.multidependency.repository.node.git.CommitRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.DeveloperRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.GitRepoRepository;
import cn.edu.fudan.se.multidependency.repository.relation.ContainRepository;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/developer")
public class DeveloperController {

    @Autowired
    private NodeService nodeService;

    @Autowired
    private ContainRepository containRepository;

    @Autowired
    private CommitRepository commitRepository;

    @Autowired
    private DeveloperRepository developerRepository;

    @Autowired
    private GitRepoRepository gitRepoRepository;

    @GetMapping("/developers")
    @ResponseBody
    public JSONArray getAllDevelopers(){
        JSONArray result = new JSONArray();
        Iterable<GitRepository> gitRepositories = gitRepoRepository.findAll();
        for(GitRepository gitRepository : gitRepositories) {
            JSONObject repositoryDetails = new JSONObject();
            List<Developer> developers = developerRepository.findDevelopersByRepository(gitRepository.getId());
            List<Integer> commitTimes = new ArrayList<>();
            for(Developer developer : developers) {
                commitTimes.add(developerRepository.queryCommitTimesByDeveloper(developer.getId()));
            }
            repositoryDetails.put("repositoryname", gitRepository.getName());
            repositoryDetails.put("language", gitRepoRepository.findGitRepositoryHasProject(gitRepository.getId()).getLanguage());
            repositoryDetails.put("developers", developers);
            repositoryDetails.put("times", commitTimes);
            result.add(repositoryDetails);
        }
        return result;
    }

    @GetMapping("/detail")
    public String getCommitsDetail(HttpServletRequest request, @RequestParam("developerId") long developerId){
        List<Commit> commits = developerRepository.queryCommitByDeveloper(developerId);
        request.setAttribute("commits", commits);
        return "history/commits";
    }

    @GetMapping("/packages")
    public String getPakcageChanged(HttpServletRequest request, @RequestParam("developerId") long developerId){
        List<Commit> commits = developerRepository.queryCommitByDeveloper(developerId);
        List<Package> packageSet = new ArrayList<>();
        List<Integer> timeSet = new ArrayList<>();
        List<DeveloperUpdateNode<Package>> packageChangedTime = new ArrayList<>();
        for(Commit commit : commits){
            List<Package> pcks = commitRepository.queryUpdatedPackageByCommitId(commit.getId());
            for(Package pck : pcks){
                if(!packageSet.contains(pck)){
                    packageSet.add(pck);
                    timeSet.add(1);
                    continue;
                }
                timeSet.set(packageSet.indexOf(pck), timeSet.get(packageSet.indexOf(pck)) + 1);
            }
        }
        for(int i = 0;i < packageSet.size(); i++){
            packageChangedTime.add(new DeveloperUpdateNode<>(packageSet.get(i), timeSet.get(i)));
        }
        Collections.sort(packageChangedTime, new Comparator<DeveloperUpdateNode<Package>>() {
            @Override
            public int compare(DeveloperUpdateNode<Package> o1, DeveloperUpdateNode<Package> o2) {
                return o2.getTimes() - o1.getTimes();
            }
        });
        request.setAttribute("packagetimes", packageChangedTime);
        return "relation/packages";
    }
}
