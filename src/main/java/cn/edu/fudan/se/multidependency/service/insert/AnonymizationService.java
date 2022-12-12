package cn.edu.fudan.se.multidependency.service.insert;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import cn.edu.fudan.se.multidependency.model.node.git.Commit;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AnonymizationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnonymizationService.class);

    private RepositoryService repository = RepositoryService.getInstance();

    private static AnonymizationService anonymizationService = new AnonymizationService();

    private Map<String, String> developerAnonymity = new ConcurrentHashMap<>();

    public static AnonymizationService getInstance() {
        return anonymizationService;
    }

    private AnonymizationService() {
    }

    public void anonymizeNodes(){
        anonymizeAllCommits(repository.getNodes().findNodesByNodeType(NodeLabelType.Commit));
        anonymizeAllDevelopers(repository.getNodes().findNodesByNodeType(NodeLabelType.Developer));
    }

    public void anonymizeAllDevelopers(List<? extends Node> developers){
        if(developers == null && developers.isEmpty()){
            return;
        }
        LOGGER.info("匿名化开发者！");
        developers.forEach(developer -> {
            anonymizeDeveloper((Developer)developer);
        });
    }

    public void anonymizeDeveloper(Developer developer){
        int size = developerAnonymity.size();
        String result= String.valueOf(size);
        switch(result.length()) {
            case 1:
                result = "00" + result;
                break;
            case 2:
                result = "0" + result;
                break;
            default:
                break;
        }
        String newDeveloperName = "User_" + result;
        developerAnonymity.put(developer.getName(), newDeveloperName);
        developer.setName(newDeveloperName);
    }

    public void anonymizeAllCommits(List<? extends Node> commits){
        if(commits == null && commits.isEmpty()){
            return;
        }
        LOGGER.info("匿名化提交信息！");
        commits.forEach(commit -> {
            anonymizeCommit((Commit) commit);
        });
    }

    public void anonymizeCommit(Commit commit){
        //根据是否匿名化实现匿名处理
        String shortMessage = commit.getShortMessage().replaceAll("[\\u4e00-\\u9fa5\\w]","*");
        String fullMessage = commit.getFullMessage().replaceAll("[\\u4e00-\\u9fa5\\w]","*");
        commit.setShortMessage(shortMessage);
        commit.setFullMessage(fullMessage);
    }
}
