package cn.edu.fudan.se.multidependency.service.insert.git;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import cn.edu.fudan.se.multidependency.service.insert.InserterForNeo4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitTimeRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;

import com.google.common.collect.Lists;

import cn.edu.fudan.se.multidependency.config.Constant;
import cn.edu.fudan.se.multidependency.utils.FileUtil;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GitExtractor implements Closeable {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(GitExtractor.class);

    private String gitProjectPath;

    private Repository repository;

    private Git git;
    
    public Ref checkout(Ref branch) {
    	try {
			git.checkout().setName(branch.getName()).call();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
    	return branch;
    }

    public GitExtractor(String gitProjectPath) {
        this.gitProjectPath = gitProjectPath;
        try {
            repository = FileRepositoryBuilder.create(new File(gitProjectPath, ".git"));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        git = new Git(repository);
    }

    public List<Ref> getBranches() {
        try {
            return git.branchList().call();
        } catch (GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Ref getCurrentBranch() {
        try {
            return repository.exactRef(org.eclipse.jgit.lib.Constants.HEAD);
        } catch ( IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getRepositoryName() {
        return FileUtil.extractFilePathName(gitProjectPath);
    }
    
    public String getGitPath() {
    	return repository.getDirectory().getAbsolutePath();
    }
    
    public String getRepositoryPath() {
    	return gitProjectPath;
    }

    public Repository getRepository() {
        return repository;
    }

    public List<RevCommit> getAllCommits() {
        try{
            Iterable<RevCommit> commits = git.log().setRevFilter(RevFilter.NO_MERGES).call();
            return Lists.newArrayList(commits.iterator());
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<RevCommit> getARangeCommitsById(String from, String to, boolean removeMerge) {
        try {
        	LogCommand command = git.log();
        	if(removeMerge) {
        		command = command.setRevFilter(RevFilter.NO_MERGES);
        	}
            Iterable<RevCommit> commits = command.addRange(repository.resolve(from),repository.resolve(to)).call();
            return Lists.newArrayList(commits.iterator());
        }catch (IOException | GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<RevCommit> getARangeCommitsByTime(String since, String until, boolean removeMerge) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.TIMESTAMP);
        try {
            Date sinceDate = simpleDateFormat.parse(since);
//            Date untilDate = simpleDateFormat.parse(until);

//            ObjectId lastCommitId = git.getRepository().resolve(org.eclipse.jgit.lib.Constants.HEAD);
//            RevWalk rw = new RevWalk(git.getRepository());
//            RevCommit latestCommit = rw.parseCommit(lastCommitId);
//            if(latestCommit.getParentCount() > 1){
//                LOGGER.error("Commit时间设定错误，最新的commit类型不能为Merge！！！");
//                return new ArrayList<>();
//            }

//            Date latestCommitTime = latestCommit.getAuthorIdent().getWhen();
//            if(latestCommitTime.after(untilDate)){
//                LOGGER.error("Commit时间设定错误，当前最新的commit时间晚于设定最新时间，请推后设定最新时间！！！");
//                return new ArrayList<>();
//            }

            RevFilter between = CommitTimeRevFilter.after(sinceDate);
            RevFilter filter = removeMerge ? AndRevFilter.create(between, RevFilter.NO_MERGES) : between;
            Iterable<RevCommit> commits = git.log().setRevFilter(filter).call();
            return Lists.newArrayList(commits.iterator());
        } catch (ParseException | GitAPIException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<Ref> getBranchesByCommitId(RevCommit revCommit) {
        try{
            return git.branchList().setContains(revCommit.getName()).call();
        }catch (GitAPIException e){
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public List<DiffEntry> getDiffBetweenCommits(RevCommit revCommit, RevCommit parentRevCommit) {
        AbstractTreeIterator currentTreeParser = prepareTreeParser(revCommit.getName());
        AbstractTreeIterator prevTreeParser = prepareTreeParser(parentRevCommit.getName());
        OutputStream outputStream = DisabledOutputStream.INSTANCE;
        try(DiffFormatter formatter = new DiffFormatter(outputStream)){
            formatter.setRepository(git.getRepository());
            formatter.setDetectRenames(true);
            formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            formatter.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM));
        	return formatter.scan( prevTreeParser,currentTreeParser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Map<DiffEntry, FileHeader> getDiffBetweenCommitsWithFileHeader(RevCommit revCommit, RevCommit parentRevCommit) {
        AbstractTreeIterator currentTreeParser = prepareTreeParser(revCommit.getName());
        AbstractTreeIterator prevTreeParser = prepareTreeParser(parentRevCommit.getName());
        OutputStream outputStream = DisabledOutputStream.INSTANCE;
        DiffFormatter formatter = null;
        List<DiffEntry> diffs = new ArrayList<>();
        try{
            formatter = new DiffFormatter(outputStream);
            formatter.setRepository(git.getRepository());
            formatter.setDetectRenames(true);
            formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            formatter.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.HISTOGRAM));
            diffs = formatter.scan( prevTreeParser,currentTreeParser);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<DiffEntry, FileHeader> result = new HashMap<>();
        for (DiffEntry diff : diffs) {
            String newPath = diff.getNewPath();
            String oldPath = diff.getOldPath();
            String changeType = diff.getChangeType().name();
            String currentPath = DiffEntry.ChangeType.DELETE.name().equals(changeType) ? oldPath : newPath;

            if (FileUtil.isFiltered(currentPath, Constant.FILE_SUFFIX)) {
                continue;
            }

            try {
                FileHeader fileHeader = formatter.toFileHeader(diff);
                result.put(diff,fileHeader);
            }catch (CorruptObjectException e) {
                e.printStackTrace();
            } catch (MissingObjectException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return result;
    }

    public List<String> getRepoFilePath(InserterForNeo4j neo4jRepository){
        List<String> filePaths = new ArrayList<>();
        List<File> files = new ArrayList<>();
        FileUtil.listFiles(repository.getWorkTree(), files);
        for (File file : files) {
            String filePath1 = file.getPath();
            if (FileUtil.isFiltered(filePath1, Constant.FILE_SUFFIX))
                continue;
            String filePath = FileUtil.extractRelativePath(filePath1, gitProjectPath);
            String databaseFilePath = "/" + getRepositoryName() + "/" + filePath;
            if(neo4jRepository.getNodes().findFileByPathRecursion(databaseFilePath) != null){
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    public List<String> getProjectFileChangeCommitIds(String gitFilePath, String since) {
        List<String> commitIds = new ArrayList<>();
        try {
            LogCommand log = git.log();
            log.addPath(gitFilePath);
            RevFilter filter = RevFilter.NO_MERGES;

            if(!"".equals(since)){
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constant.TIMESTAMP);
                Date sinceDate = simpleDateFormat.parse(since);

                RevFilter after = CommitTimeRevFilter.after(sinceDate);
                filter = AndRevFilter.create(filter, after);
            }
            Iterable<RevCommit> logMsgs = log.setRevFilter(filter).call();
            List<RevCommit> commits = Lists.newArrayList(logMsgs.iterator());

            Collections.sort(commits, new Comparator<RevCommit>() {
                @Override
                public int compare(RevCommit o1, RevCommit o2) {
                    return o2.getCommitTime()-o1.getCommitTime();
                }
            });

            commits.forEach(revCommit -> {
                commitIds.add(revCommit.getName());
            });
        } catch (NoHeadException e) {
            e.printStackTrace();
        } catch (GitAPIException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return commitIds;
    }

    private CanonicalTreeParser prepareTreeParser(String objectId) {
        CanonicalTreeParser treeParser = new CanonicalTreeParser();
        try (RevWalk revWalk = new RevWalk(repository)) {
            RevCommit commit = revWalk.parseCommit(ObjectId.fromString(objectId));
            RevTree tree = revWalk.parseTree(commit.getTree().getId());
            ObjectReader oldReader = repository.newObjectReader();
            treeParser.reset(oldReader, tree.getId());
            revWalk.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return treeParser;
    }

    public RevCommit getCommitByCommitId(String objectId) {
        RevCommit commit = null;
        try (RevWalk revWalk = new RevWalk(repository)) {
            commit = revWalk.parseCommit(ObjectId.fromString(objectId));
            revWalk.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return commit;
    }

    public List<String> getCommitFilesPath(RevCommit commit) {
        try (TreeWalk treeWalk = new TreeWalk(repository)) {
            treeWalk.addTree(commit.getTree());
            treeWalk.setRecursive(true);
            List<String> result = new ArrayList<>();
            while (treeWalk.next()) {
                String path = treeWalk.getPathString();
                result.add(path);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Set<Integer> getRelationBtwCommitAndIssue(RevCommit commit) {
        String issueNumRegex = "#[1-9][0-9]*";
        Set<Integer> issueNumFromShort = getMatcher(issueNumRegex, commit.getShortMessage());
        Set<Integer> issueNumFromFull = getMatcher(issueNumRegex, commit.getFullMessage());
        issueNumFromShort.addAll(issueNumFromFull);
        return issueNumFromShort;
    }

    public Set<Integer> getMatcher(String regex, String source) {
        Set<Integer> result = new HashSet<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            result.add(Integer.parseInt(matcher.group().substring(1)));
        }
        return result;
    }

    public Set<Integer> getRelationBtwCommitAndJiraIssue(RevCommit commit) {
        String shortMessage = commit.getShortMessage();
        String fullMessage = commit.getFullMessage();
        shortMessage = (shortMessage == null ? "" : shortMessage.replaceAll("\\s"," "));
        fullMessage = (fullMessage == null ? "" : fullMessage.replaceAll("\\s"," "));

        String issueNumRegex = "[^\\[\\s][A-Za-z_][\\w]*-[1-9][0-9]*[\\]\\s$]";
//        Set<Integer> issueNumFromShort = getMatcherForJira(issueNumRegex, shortMessage);
        Set<Integer> issueNumFromFull = getMatcherForJira(issueNumRegex, fullMessage);
//        issueNumFromShort.addAll(issueNumFromFull);
        String newIssueNumRegex = "^(\\[)[1-9][0-9]*(\\])(\\s)";
        Set<Integer> newIssueNumFromShort = getMatcherForJiraNew(newIssueNumRegex, shortMessage);
        issueNumFromFull.addAll(newIssueNumFromShort);
        return issueNumFromFull;

    }

    public Set<Integer> getMatcherForJira(String regex, String source) {
        Set<Integer> result = new HashSet<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String tmp = matcher.group();
            int endIndex = tmp.length();
            if(tmp.endsWith("]") || tmp.endsWith(" ")){
                endIndex = endIndex - 1;
            }
            result.add(Integer.parseInt(tmp.substring(tmp.lastIndexOf("-") + 1, endIndex)));
        }
        return result;
    }

    public Set<Integer> getMatcherForJiraNew(String regex, String source) {
        Set<Integer> result = new HashSet<>();
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(source);
        while (matcher.find()) {
            String tmp = matcher.group();
            result.add(Integer.parseInt(tmp.substring(tmp.lastIndexOf("[") + 1, tmp.lastIndexOf("]"))));
        }
        return result;
    }

    public void close() {
    	if(git != null) {
    		git.close();
    	}
    	if(repository != null) {
    		repository.close();
    	}
    }
}
