package cn.edu.fudan.se.multidependency.service.insert.git;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.insert.ThreadService;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.lib.Ref;;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Branch;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.model.node.git.GitRepository;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.model.relation.Contain;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitAddressIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitInheritCommit;
import cn.edu.fudan.se.multidependency.model.relation.git.CommitUpdateFile;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperReportIssue;
import cn.edu.fudan.se.multidependency.model.relation.git.DeveloperSubmitCommit;
import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import cn.edu.fudan.se.multidependency.utils.config.GitConfig;

public class EvolutionExtractor extends ExtractorForNodesAndRelationsImpl {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionExtractor.class);

    private GitExtractor gitExtractor;

    private Map<Integer, Issue> issues;

    private Map<String, Set<Issue>> commitToIssues;

    private GitConfig gitConfig;
    
    private List<Ref> branches = null;
    
    private GitRepository gitRepository;
    
    private Commit currentVersionCommit = null;
    
    private String currentVersionCommitId;
    
    public EvolutionExtractor(GitConfig gitConfig) {
        this.gitConfig = gitConfig;
        this.gitExtractor = new GitExtractor(gitConfig.getPath());
        this.branches = gitExtractor.getBranches();
        this.currentVersionCommitId = gitConfig.getCurrentVersionCommitId();
    }

    @Override
    public void addNodesAndRelations() throws Exception {
        //添加gitRepository节点和gitRepository到project的包含关系
        gitRepository = new GitRepository(generateEntityId(), gitExtractor.getRepositoryName(), gitExtractor.getGitPath(), gitExtractor.getRepositoryPath());
        addNode(gitRepository, null);
        List<Project> projectList = this.getNodes().findProject(gitExtractor.getRepositoryName());
        if(!projectList.isEmpty()) {
            projectList.forEach( project -> {
                addRelation(new Contain(gitRepository , project));
            });
        }

        LOGGER.info(gitExtractor.getGitPath() + " " + gitExtractor.getRepositoryPath() + " " + gitExtractor.getRepositoryName() + " " + gitRepository.getPath());

        if(gitConfig.getIssueFrom() != null && !gitConfig.getIssueFrom().isEmpty()){
            String issueFrom = gitConfig.getIssueFrom().toLowerCase();
            if(Constant.ISSUE_FROM_JIRA.equals(issueFrom)){
                addJiraIssues();
            }else if(Constant.ISSUE_FROM_GITHUB.equals(issueFrom)){
                addIssues();
            }
        }

        if(!gitConfig.getBranches().isEmpty()) {
        	addSpecificBranches();
        } else {
            addHeadsBranch();
//            addAllBranches();
        }
        
        close();
    }
    
    private void addSpecificBranches() throws Exception {
        Set<String> branchNames = gitConfig.getBranches();
    	for(Ref branch : branches) {
    		String name = branch.getName();
    		name = name.substring("refs/heads/".length());
    		if(!branchNames.contains(name)) {
    			continue;
    		}
    		gitExtractor.checkout(branch);
    		Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
    		addNode(branchNode, null);
    		addRelation(new Contain(gitRepository, branchNode));
    		addCommitsAndRelations(branchNode);
    	}
    }

    private void addHeadsBranch() throws Exception {
        Ref branch = gitExtractor.getCurrentBranch();
        if(branch != null) {
            Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
            addNode(branchNode, null);
            addRelation(new Contain(gitRepository, branchNode));
            addCommitsAndRelations(branchNode);
        }else {
            LOGGER.error("Get HeadsBranch Error");
        }
    }

    
    private void addCommitsAndRelations(Branch branch) throws Exception {
        List<RevCommit> commits = null;
        String commitTimeSince = "";
        if (!gitConfig.isSpecifyCommitRange()) {
            commits = gitExtractor.getAllCommits();
        } else {
            if (gitConfig.isSpecifyByCommitId()) {
                commits = gitExtractor.getARangeCommitsById(gitConfig.getCommitIdFrom(), gitConfig.getCommitIdTo(), true);
                RevCommit commitIdFrom = gitExtractor.getCommitByCommitId(gitConfig.getCommitIdFrom());
                Date revTime =new Date((long)commitIdFrom.getCommitTime()*1000);
                SimpleDateFormat time = new SimpleDateFormat(Constant.TIMESTAMP);
                commitTimeSince = time.format(revTime);
            } else {
                commits = gitExtractor.getARangeCommitsByTime(gitConfig.getCommitTimeSince(), gitConfig.getCommitTimeUntil(), true);
                commitTimeSince = gitConfig.getCommitTimeSince();
            }
        }
        int beforeReleaseCommits = 0;
        int afterReleaseCommits = 0;
        LOGGER.info(gitRepository.getName() + ", commit 数量：" + commits.size());
//      Collections.reverse(commits);

        Collections.sort(commits, new Comparator<RevCommit>() {
            @Override
            public int compare(RevCommit o1, RevCommit o2) {
                return o2.getCommitTime()-o1.getCommitTime();
            }
        });

        LOGGER.info("根据Git库当前文件，获取所有文件的变更历史索引（记录开始时间)...");
        Map<String, Set<String>> file2FormerPathMap = new HashMap<>();
        Map<String, Set<String>> former2filePathMap = new HashMap<>();
        List<File> files = new ArrayList<>();
        FileUtil.listFiles(gitExtractor.getRepository().getWorkTree(), files);
        for (File file : files) {
            String gitFilePath = file.getPath();
            if (FileUtil.isFiltered(gitFilePath, Constant.FILE_SUFFIX))
                continue;
            String filePath = FileUtil.extractRelativePath(gitFilePath, gitConfig.getPath());
            String databaseFilePath = "/" + gitExtractor.getRepositoryName() + "/" + filePath;
            ProjectFile projectFile = this.getNodes().findFileByPathRecursion(databaseFilePath);
            if(projectFile != null){
                file2FormerPathMap.put(filePath, new HashSet<>());
            }
        }

        ThreadService threadService = new ThreadService();
        Map<String, List<String>> file2CommitIds = new HashMap<>(threadService.fileChangeCommitsAnalyse(file2FormerPathMap.keySet(),gitExtractor,commitTimeSince));
//        for (String path : file2FormerPathMap.keySet()){
//            file2CommitIds.put(path,gitExtractor.getProjectFileChangeCommitIds(path, commitTimeSince));
//        }

        Map<String, List<String>> commitId2ChangeFiles = new HashMap<>();
        file2CommitIds.forEach((path,commitIds)->{
            commitIds.forEach(commitId ->{
                List<String> filePaths =  commitId2ChangeFiles.getOrDefault(commitId, new ArrayList<>());
                filePaths.add(path);
                commitId2ChangeFiles.put(commitId, filePaths);
            });
        });
        LOGGER.info("根据Git库当前文件，获取所有文件的变更历史索引(记录结束时间)");

        for (RevCommit revCommit : commits) {
        	String authoredDate = new SimpleDateFormat(Constant.TIMESTAMP).format(revCommit.getAuthorIdent().getWhen());
            Date revDate = new Date((long)revCommit.getCommitTime()*1000);
            String commitDate = new SimpleDateFormat(Constant.TIMESTAMP).format(revDate);

        	boolean merge = revCommit.getParentCount() > 1;
        	Commit commit = null;
            String shortMessage = revCommit.getShortMessage();
            String fullMessage = revCommit.getFullMessage();
            fullMessage = (fullMessage == null ? "" : fullMessage.replaceAll("\\s"," "));

        	if(branch != null) {
        		commit = this.getNodes().findCommitByCommitId(revCommit.getName());
        		if(commit != null) {
        			addRelation(new Contain(branch, commit));
        			continue;
        		} 
        		//添加commit节点

        		commit = new Commit(generateEntityId(), revCommit.getName(), shortMessage,
                        fullMessage, authoredDate, commitDate, revCommit.getCommitTime(), merge);
        		addNode(commit, null);
        		addRelation(new Contain(branch, commit));
        	} else {
        		commit = new Commit(generateEntityId(), revCommit.getName(), shortMessage,
                        fullMessage, authoredDate, commitDate, revCommit.getCommitTime(), merge);
        		addNode(commit, null);
        		//添加branch到commit的包含关系
        		List<Ref> branchesOfCommit = gitExtractor.getBranchesByCommitId(revCommit);
        		for (Ref refBranch : branchesOfCommit) {
        			Branch branchNode = this.getNodes().findBranchByBranchId(refBranch.getObjectId().toString());
        			if (branchNode == null) {
        				throw new Exception(refBranch.getName() + "is non-existent");
        			}
        			addRelation(new Contain(branchNode, commit));
        		}
        	}
        	
        	if(commit.getCommitId().equals(this.currentVersionCommitId)) {
        		System.out.println(commit.getCommitId());
        		this.currentVersionCommit = commit;
        	}
        	
        	if(currentVersionCommit != null && currentVersionCommit.getCommitTime() > commit.getCommitTime()) {
        		commit.setUsingForIssue(false);
        		beforeReleaseCommits++;
        	} else {
        		afterReleaseCommits++;
        	}
            
            //添加developer节点和developer到commit的关系
            Developer developer = this.getNodes().findDeveloperByName(revCommit.getAuthorIdent().getName());
            if (developer == null) {
                developer = new Developer(generateEntityId(), revCommit.getAuthorIdent().getName());
                developer.addDeveloperRole(Constant.DEVELOPER_ROLE_CODER);
                addNode(developer, null);
            }
            addRelation(new DeveloperSubmitCommit(developer, commit));

            List<String> changeFiles = commitId2ChangeFiles.get(revCommit.getName());
            Map<String, CommitUpdateFile> fileToCommitUpdateFileRelations = new HashMap<>();
            //添加changeFiles到commit的关系
            if(changeFiles != null && !changeFiles.isEmpty()) {
                for (String changeFile : changeFiles) {
                    String relationFilePath = "/" + gitExtractor.getRepositoryName() + "/" + changeFile;
                    CommitUpdateFile update = createCommitUpdateFileRelation(relationFilePath, commit, "", 0, 0);
                    fileToCommitUpdateFileRelations.put(changeFile, update);
                }
            }

            //添加commit到commit的继承关系
            if(revCommit.getParentCount() > 0){
                RevCommit[] parentRevCommits = revCommit.getParents();
                for (RevCommit parentRevCommit : parentRevCommits) {
//                	Commit parentCommit = this.getNodes().findCommitByCommitId(parentRevCommit.getName());
//                	if (parentCommit != null) {
//                		addRelation(new CommitInheritCommit(commit, parentCommit));
//                	}

                	//当commit为Merge，或者当前commit不关联目标分析文件时，跳过
                    if(changeFiles == null || revCommit.getParentCount() > 1){
                        continue;
                    }
                    //添加commit到file的更新关系
                    Map<DiffEntry, FileHeader> diffs = gitExtractor.getDiffBetweenCommitsWithFileHeader(revCommit, parentRevCommit);
                    for(Map.Entry<DiffEntry, FileHeader> entry : diffs.entrySet()) {
                        DiffEntry diff = entry.getKey();
                        String changeType = diff.getChangeType().name();
                        String diffFilePath = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? diff.getOldPath() : diff.getNewPath();

                        String projectFilePath = diffFilePath;
                        Set<String> formerPaths = file2FormerPathMap.get(diffFilePath);
                        Set<String> filePaths = former2filePathMap.get(diffFilePath);
                        if (formerPaths == null && filePaths == null) {
                            continue;
                        } else if (filePaths != null && !filePaths.isEmpty()) {
                            boolean isUse = false;
                            for (String filePath : filePaths) {
                                if (changeFiles.contains(filePath)) {
                                    projectFilePath = filePath;
                                    isUse = true;
                                    break;
                                }
                            }
                            if (!isUse){
                                continue;
                            }
                        }

                        String finalProjectFilePath = projectFilePath;
                        List<String> commitIds = file2CommitIds.get(finalProjectFilePath);
                        CommitUpdateFile update = fileToCommitUpdateFileRelations.get(finalProjectFilePath);

                        if(commitIds != null && !commitIds.isEmpty() && update !=null && commitIds.contains(revCommit.getName())){
                            commitIds.removeIf(value -> value.equals(revCommit.getName()));
                            file2CommitIds.put(finalProjectFilePath, commitIds);
                            changeFiles.removeIf(value -> value.equals(finalProjectFilePath));

                            FileHeader fileHeader = entry.getValue();
                            List<HunkHeader> hunks = (List<HunkHeader>) fileHeader.getHunks();
                            int addSize = 0;
                            int subSize = 0;
                            for(HunkHeader hunkHeader:hunks){
                                EditList editList = hunkHeader.toEditList();
                                for(Edit edit : editList){
                                    subSize += edit.getEndA()-edit.getBeginA();
                                    addSize += edit.getEndB()-edit.getBeginB();
                                }
                            }
                            update.setAddLines(addSize);
                            update.setSubLines(subSize);
                            update.setUpdateType(changeType);
                            fileToCommitUpdateFileRelations.put(finalProjectFilePath,update);

                            if(commitIds.isEmpty()){
                                file2FormerPathMap.keySet().removeIf(key -> key.equals(finalProjectFilePath));
                                file2CommitIds.keySet().removeIf(value -> value.equals(finalProjectFilePath));
                                continue;
                            }

                            if(DiffEntry.ChangeType.RENAME.name().equals(changeType) || DiffEntry.ChangeType.COPY.name().equals(changeType)){
                                if(!(diff.getNewPath().equals(diff.getOldPath())) && !finalProjectFilePath.equals(diff.getOldPath())){
                                    Set<String> newFormerPaths = file2FormerPathMap.get(finalProjectFilePath);
                                    newFormerPaths.add(diff.getOldPath());
                                    file2FormerPathMap.put(finalProjectFilePath, newFormerPaths);
                                    Set<String> newPjFilePaths = former2filePathMap.getOrDefault(diff.getOldPath(),new HashSet<>());
                                    newPjFilePaths.add(finalProjectFilePath);
                                    former2filePathMap.put(diff.getOldPath(),newPjFilePaths);
                                }
                            }
                        }else {
                            continue;
                        }
                    }


                }
            } else {
                List<String> filesPath = gitExtractor.getCommitFilesPath(revCommit);
                for (String path : filesPath) {
                    if (FileUtil.isFiltered(path, Constant.FILE_SUFFIX)) continue;

                    String projectFilePath = path;
                    Set<String> formerPaths = file2FormerPathMap.get(path);
                    Set<String> filePaths = former2filePathMap.get(path);
                    if (formerPaths == null && filePaths == null) {
                        continue;
                    } else if (filePaths != null && !filePaths.isEmpty()) {
                        boolean isUse = false;
                        for (String filePath : filePaths) {
                            if (changeFiles.contains(filePath)) {
                                projectFilePath = filePath;
                                isUse = true;
                                break;
                            }
                        }
                        if (!isUse){
                            continue;
                        }
                    }

                    String finalProjectFilePath = projectFilePath;
                    List<String> commitIds = file2CommitIds.get(finalProjectFilePath);
                    CommitUpdateFile update = fileToCommitUpdateFileRelations.get(finalProjectFilePath);

                    if(commitIds != null && !commitIds.isEmpty() && update != null && commitIds.contains(revCommit.getName())){
                        update.setUpdateType(DiffEntry.ChangeType.ADD.name());
                        changeFiles.removeIf(value -> value.equals(finalProjectFilePath));
                    }
                }
            }

            //插入CommitUpdateFile关系到关系库中
            fileToCommitUpdateFileRelations.forEach((file, update) ->{
                addRelation(update);
            });


            if(changeFiles != null && !changeFiles.isEmpty()){
                LOGGER.warn("Commit update file分析不准确，commitid is：" + revCommit.getName());
                changeFiles.forEach( changeFile -> {
                    List<String> commitIds = file2CommitIds.get(changeFile);
                    if(commitIds != null){
                        commitIds.removeIf(value -> value.equals(revCommit.getName()));
                        file2CommitIds.put(changeFile, commitIds);
                    }
                    LOGGER.warn("影响文件为: " + changeFile);
                });
            }
            
            //添加Commit到Issue的关系
            if (issues != null && !issues.isEmpty() && commit.isUsingForIssue() && !commit.isMerge()) {
                if(Constant.ISSUE_FROM_GITHUB.equals(gitConfig.getIssueFrom().toLowerCase())){
                    Collection<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndIssue(revCommit);
                    for (Integer issueNum : issuesNum) {
                        if (issues.containsKey(issueNum)) {
                            addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                        }
                    }
                }

                if(Constant.ISSUE_FROM_JIRA.equals(gitConfig.getIssueFrom().toLowerCase())){
                    //通过issueLink 进行关联
                    Set<Issue> issueSet = commitToIssues.get(commit.getCommitId());
                    if(issueSet != null && issueSet.size() > 0){
                        Commit finalCommit = commit;
                        issueSet.forEach( (issue ->{
                            addRelation(new CommitAddressIssue(finalCommit, issue));
                        }));
                    }

                    //通过commit中的 issueKey进行关联
                    Collection<Integer> issuesNum = gitExtractor.getRelationBtwCommitAndJiraIssue(revCommit);
                    for (Integer issueNum : issuesNum) {
                        if (issues.containsKey(issueNum)) {
                            addRelation(new CommitAddressIssue(commit, issues.get(issueNum)));
                        }
                    }
                }

            }
        }
        System.out.println(gitRepository.getName() + " " + beforeReleaseCommits + ", " + afterReleaseCommits);
    }
    
    private CommitUpdateFile createCommitUpdateFileRelation(String filePath, Commit commit, String updateType,int addLines, int subLines) {
        ProjectFile file = this.getNodes().findFileByPathRecursion(filePath);
        CommitUpdateFile update = null;
        if (file != null) {
        	update = new CommitUpdateFile(commit, file, updateType);
            update.setAddLines(addLines);
            update.setSubLines(subLines);
        }
        return update;
    }

    private void addAllBranches() throws Exception {
        //添加branch节点和gitRepository到branch的包含关系
        List<Ref> branches = gitExtractor.getBranches();
        for (Ref branch : branches) {
            Branch branchNode = new Branch(generateEntityId(), branch.getObjectId().toString(), branch.getName());
            addNode(branchNode, null);
            addRelation(new Contain(gitRepository, branchNode));
        }
    	addCommitsAndRelations(null);
    }
    
    private void addIssues() throws Exception {
    	//添加issue节点和gitRepository到issue的包含关系
    	IssueExtractor issueExtractor = new IssueExtractor(gitConfig.getIssueFilePathes());
    	issues = issueExtractor.extract();
    	Map<Integer, Issue> newIssues = issueExtractor.newIssues();
    	System.out.println("newIssues size: " + newIssues.size());
    	for (Issue issue : newIssues.values()) {
    		issue.setEntityId(generateEntityId());
    		addNode(issue, null);
    		addRelation(new Contain(gitRepository, issue));
    		
    		//添加developer节点和developer到issue的关系
    		Developer developer = this.getNodes().findDeveloperByName(issue.getDeveloperName());
    		if (developer == null) {
    			developer = new Developer(generateEntityId(), issue.getDeveloperName());
    			developer.addDeveloperRole(Constant.DEVELOPER_ROLE_ISSUE_REPORTER);
    			addNode(developer, null);
    		}
    		addRelation(new DeveloperReportIssue(developer, issue));
    	}
    }

    private void addJiraIssues() throws Exception {
        //添加issue节点和gitRepository到issue的包含关系
        JiraIssueExtractor issueExtractor = new JiraIssueExtractor(gitConfig.getIssueFilePathes());
        issues = issueExtractor.extract();
        commitToIssues = issueExtractor.getCommitToIssues();
        Map<Integer, Issue> newIssues = issueExtractor.newIssues();
        System.out.println("newIssues size: " + newIssues.size());
        for (Issue issue : newIssues.values()) {
            issue.setEntityId(generateEntityId());
            issue.setRepoBelongName(gitExtractor.getRepositoryName());
            addNode(issue, null);
            addRelation(new Contain(gitRepository, issue));

            //添加developer节点和developer到issue的关系
            Developer developer = this.getNodes().findDeveloperByName(issue.getReporter());
            if (developer == null) {
                developer = new Developer(generateEntityId(), issue.getDeveloperName());
                developer.addDeveloperRole(Constant.DEVELOPER_ROLE_ISSUE_REPORTER);
                addNode(developer, null);
            }
            addRelation(new DeveloperReportIssue(developer, issue));
        }
    }
    
    private void close() {
    	if(gitExtractor != null) {
    		gitExtractor.close();
    	}
    }
}
