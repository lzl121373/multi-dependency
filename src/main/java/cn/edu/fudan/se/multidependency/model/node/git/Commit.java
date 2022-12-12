package cn.edu.fudan.se.multidependency.model.node.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Transient;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Commit implements Node {

	private static final long serialVersionUID = 2244271646952758656L;

    @Id
    @GeneratedValue
    private Long id;
    
    private Long entityId;
    
    private String commitId;

    private String shortMessage;

    private String fullMessage;

	private String authoredDate;

	private String commitDate;
	
	private int commitTime;
	
	private int commitFilesSize = -1;
	
	private boolean merge;
	
	private boolean usingForIssue;

	public Commit(Long entityId, String commitId, String shortMessage, 
			String fullMessage, String authoredDate, String commitDate,
				  int commitTime, boolean merge){
		this.entityId = entityId;
		this.commitId = commitId;
		this.shortMessage = shortMessage;
		this.fullMessage = fullMessage;
		this.authoredDate = authoredDate;
		this.commitDate = commitDate;
		this.commitTime = commitTime;
		this.merge = merge;
		this.usingForIssue = true;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? "" : getEntityId());
	    properties.put("commitId", getCommitId() == null ? "" : getCommitId());
	    properties.put("shortMessage", getShortMessage() == null ? "" : getShortMessage());
	    properties.put("fullMessage", getFullMessage() == null ? "" : getFullMessage());
		properties.put("authoredDate", getAuthoredDate() == null ? "" : getAuthoredDate());
		properties.put("commitDate", getCommitDate() == null ? "" : getCommitDate());
		properties.put("commitTime", getCommitTime());
		properties.put("commitFilesSize", getCommitFilesSize());
		properties.put("usingForIssue", isUsingForIssue());
		properties.put("merge", isMerge());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Commit;
	}
	
	@Override
	public String getName() {
		return commitId;
	}


}
