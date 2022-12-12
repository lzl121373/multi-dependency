package cn.edu.fudan.se.multidependency.service.insert.git;

import cn.edu.fudan.se.multidependency.model.IssueType;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.utils.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JiraIssueExtractor {

    private static final Logger LOGGER = LoggerFactory.getLogger(JiraIssueExtractor.class);

    private Collection<String> issueFilePathes;

    private Map<Integer, Issue> newIssues = new HashMap<>();

    private Map<String, Set<Issue>> commitToIssues = new HashMap<>();

    private static final Map<String, Map<Integer, Issue>> sameIssuePath = new ConcurrentHashMap<>();

    public JiraIssueExtractor(Collection<String> issueFilePathes) {
    	this.issueFilePathes = issueFilePathes;
    }

    public static void main(String[] args) {
        String issueFilePath = "D:\\workspace\\archdebt\\issues\\CASSANDRA_Bug_1000.json";
        Collection<String> issueFilePathes = new ArrayList<>();
        issueFilePathes.add(issueFilePath);
        JiraIssueExtractor jiraIssueExtractor = new JiraIssueExtractor(issueFilePathes);
        Map<Integer, Issue> issueMap = new HashMap<>();
        try{
            issueMap = jiraIssueExtractor.extract(issueFilePath);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println(issueMap.size());

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
                temp.forEach( (key, issue) -> {
                    String commitId = issue.getCommitLinkId();
                    if(commitId != null && !(commitId.isEmpty())){
                        Set<Issue> issueSet = commitToIssues.getOrDefault(commitId, new HashSet<>());
                        issueSet.add(issue);
                        commitToIssues.put(commitId, issueSet);
                    }
                });
    			sameIssuePath.put(issueFilePath, temp);
    		}
    	}
    	return result;
    }

	public Map<Integer, Issue> newIssues() {
		return newIssues;
	}

    public Map<String, Set<Issue>> getCommitToIssues() {
        return commitToIssues;
    }

    private Map<Integer, Issue> extract(String issueFilePath) throws Exception {
        Map<Integer,Issue> result = new HashMap<>();
        File file = new File(issueFilePath);
        JSONArray issues = JSONUtil.extractJSONArray(file);
        for(int i = 0; i < issues.size(); i++) {
            JSONObject issueJson = issues.getJSONObject(i);
            JSONObject fields = issueJson.getJSONObject("fields");
            Issue issue = new Issue();
            String issueKey = issueJson.getString("key");
            issue.setIssueKey(issueKey);
            String key = "-1";
            if(issue != null){
                try {
                    key = issueKey.substring(issueKey.lastIndexOf("-") + 1);
                    issue.setIssueId(Integer.parseInt(key));
                }catch (Exception e){
                    e.printStackTrace();
                    LOGGER.error("Jira issue key parsing error, the key is " + issueKey);
                }
            }

            issue.setIssueUrl(issueJson.getString("self"));

            JSONObject issueTypeObject =  fields.getJSONObject("issuetype");
            String type = issueTypeObject == null ? "" : issueTypeObject.getString("name");
            issue.setType(IssueType.typeOfIssue(type));

            String summary = fields.getString("summary");
            summary = (summary == null ? "" : summary.replaceAll("\\s"," "));;
            issue.setTitle(summary);

            JSONObject statusObject =  fields.getJSONObject("status");
            String status = statusObject == null ? "" : statusObject.getString("name");
            issue.setStatus(status);

            JSONObject resolutionObject =  fields.getJSONObject("resolution");
            String resolution = resolutionObject == null ? "" : resolutionObject.getString("name");
            issue.setResolution(resolution);

            JSONObject priorityObject =  fields.getJSONObject("priority");
            String priority = priorityObject == null ? "" : priorityObject.getString("name");
            issue.setPriority(priority);

            String commitLinkUrl = fields.getString("customfield_12313924");
            commitLinkUrl = commitLinkUrl == null ? "" : commitLinkUrl;
            issue.setCommitLinkUrl(commitLinkUrl);

            issue.setCreateTime(fields.getString("created"));
            issue.setUpdateTime(fields.getString("updated"));
            issue.setCloseTime(fields.getString("resolutiondate"));

            JSONObject reporterObject =  fields.getJSONObject("reporter");
            String reporter = reporterObject == null ? "" : reporterObject.getString("displayName");
            issue.setReporter(reporter);

            JSONArray issuelinksArray = fields.getJSONArray("issuelinks");
            String issuelinks = (issuelinksArray == null || issuelinksArray.size() < 1 ) ? "" : issuelinksArray.getJSONObject(0).getString("displayName");
            issue.setIssueLinks(issuelinks);

            String issueDescription = fields.getString("description");
            issueDescription = (issueDescription == null ? "" : issueDescription.replaceAll("\\s"," "));
            issue.setDescription(issueDescription);

            issue.setDeveloperName(reporter);

            String commitLinkId = commitLinkUrl == null ? "" : commitLinkUrl.substring(commitLinkUrl.lastIndexOf("/") +1);

            if(commitLinkId.length() < 45 && commitLinkId.length() > 35){
                issue.setCommitLinkId(commitLinkId);
            }else{
                issue.setCommitLinkId("");
            }

            result.put(issue.getIssueId(),issue);
        }
        return result;
    }
}
