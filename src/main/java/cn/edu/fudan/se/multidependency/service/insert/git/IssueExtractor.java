package cn.edu.fudan.se.multidependency.service.insert.git;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;

public class IssueExtractor {

    private Collection<String> issueFilePathes;
    
    private Map<Integer, Issue> newIssues = new HashMap<>();
    
    private static final Map<String, Map<Integer, Issue>> sameIssuePath = new ConcurrentHashMap<>();

    public IssueExtractor(Collection<String> issueFilePathes) {
    	this.issueFilePathes = issueFilePathes;
    }
    
    public synchronized Map<Integer, Issue> extract() throws Exception {
    	Map<Integer, Issue> result = new HashMap<>();
    	for(String issueFilePath : this.issueFilePathes) {
    		if(sameIssuePath.containsKey(issueFilePath)) {
    			result.putAll(sameIssuePath.get(issueFilePath));
    		} else {
    			Map<Integer, Issue> temp = extract(issueFilePath);
    			result.putAll(temp);
    			newIssues.putAll(temp);
    			sameIssuePath.put(issueFilePath, temp);
    		}
    	}
    	return result;
    }

	public Map<Integer, Issue> newIssues() {
		return newIssues;
	}

    private Map<Integer, Issue> extract(String issueFilePath) throws Exception {
        Map<Integer,Issue> result = new HashMap<>();
        File file = new File(issueFilePath);
        JSONArray issues = JSONUtil.extractJSONArray(file);
        for(int i = 0; i < issues.size(); i++) {
            JSONObject issueJson = issues.getJSONObject(i);
            Issue issue = new Issue(issueJson.getInteger("number"), issueJson.getString("title"), issueJson.getString("state"),
                    issueJson.getString("html_url"), issueJson.getString("created_at"), issueJson.getString("updated_at"),
                    issueJson.getString("closed_at"), issueJson.getString("body"));
            JSONObject user = issueJson.getJSONObject("user");
            if(user == null) {
                continue;
            }
            issue.setDeveloperName(user.getString("login"));
            result.put(issue.getIssueId(),issue);
        }
        return result;
    }
}
