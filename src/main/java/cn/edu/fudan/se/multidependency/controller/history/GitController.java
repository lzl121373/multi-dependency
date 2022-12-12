package cn.edu.fudan.se.multidependency.controller.history;

import java.util.Collection;

import cn.edu.fudan.se.multidependency.service.query.history.data.GitRepoMetric;
import cn.edu.fudan.se.multidependency.service.query.metric.MetricShowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.relation.git.CoChange;
import cn.edu.fudan.se.multidependency.service.query.history.GitAnalyseService;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/git")
public class GitController {

    @Autowired
    GitAnalyseService gitAnalyseService;
    
    @Autowired
    NodeService nodeService;
    
    @Autowired
    ContainRelationService containRelationService;

    @Autowired
    MetricShowService metricShowService;

    @GetMapping(value = {"", "/", "/index"})
    public String index(HttpServletRequest request) {
        //request.setAttribute("commits", commitService.queryAllCommits());
        return "git";
    }

    @GetMapping("/repoMetric")
    @ResponseBody
    public Object getGitRepoMetric() {
        return metricShowService.getGitRepoMetrics();
    }
    
    @GetMapping("/repo/commit")
    @ResponseBody
    public GitRepository queryGitRepoByCommit(@RequestParam("commitId") long commitId) {
    	Node node = nodeService.queryNodeById(commitId);
    	if(node == null || !(node instanceof Commit)) {
    		return null;
    	}
    	Commit commit = (Commit) node;
    	GitRepository result = containRelationService.findCommitBelongToGitRepository(commit);
    	return result;
    }
    
//    @GetMapping("/cochange/commits")
//    @ResponseBody
//    public Collection<Commit> findCommitsByCoChange(@RequestParam("cochangeId") long cochangeId) {
//    	CoChange cochange = gitAnalyseService.findCoChangeById(cochangeId);
//    	System.out.println(cochange);
//    	if(cochange == null) {
//    		return new ArrayList<>();
//    	}
//    	return gitAnalyseService.findCommitsByCoChange(cochange);
//    }

    @GetMapping("/developerToMicroservice")
    @ResponseBody
    public JSONObject cntOfDevUpdMs() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.cntOfDevUpdMsList());
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
    @GetMapping("/topKFileBeUpd")
    @ResponseBody
    public JSONObject topKFileBeUpd() {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            result.put("value", gitAnalyseService.getTopKFileBeUpd(10));
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
    @GetMapping("/topKFileCoChange")
    @ResponseBody
    public JSONObject topKFileCoChange(@RequestParam(name="k", required=false, defaultValue="10") int k) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", "success");
            Collection<CoChange> value = null;
            if(k > 0) {
            	value = gitAnalyseService.getTopKFileCoChange(k);
            } else {
            	value = gitAnalyseService.calCntOfFileCoChange();
            }
            result.put("value", value);
        } catch (Exception e) {
            result.put("result", "fail");
            result.put("msg", e.getMessage());
        }
        return result;
    }
}
