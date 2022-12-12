package cn.edu.fudan.se.multidependency.model.node.git;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.NodeLabelType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.neo4j.ogm.annotation.Transient;

@Data
@NodeEntity
@NoArgsConstructor
@EqualsAndHashCode
public class Issue implements Node {

	private static final long serialVersionUID = 4701956188777508218L;

	@Id
	@GeneratedValue
	private Long id;

	private Long entityId;

	private String repoBelongName;

	private int issueId;

	private String issueKey;

	private String issueUrl;

	private String type;

	private String title;
	
	private String status;

	private String resolution;

	private String priority;

	private String commitLinkUrl;

	private String commitLinkId;

	private String createTime;
	
	private String updateTime;
	
	private String closeTime;

	private String reporter;

	private String issueLinks;

	private String description;

	@Transient
	private String developerName;

	public Issue(int issueId, String title, String status, String commitLinkUrl,
				 String createTime, String updateTime, String closeTime, String description) {
		this.issueId = issueId;
		this.title = title;
		this.status = status;
		this.commitLinkUrl = commitLinkUrl;
		this.createTime = createTime;
		this.updateTime = updateTime;
		this.closeTime = closeTime;
		this.description = description;
	}

	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties = new HashMap<>();
		properties.put("entityId", getEntityId() == null ? -1 : getEntityId());
		properties.put("issueId", getIssueId());
		properties.put("repoBelongName", getRepoBelongName() == null ? "" : getRepoBelongName());
		properties.put("issueKey", getIssueKey() == null ? "" : getIssueKey());
		properties.put("issueUrl", getIssueUrl() == null ? "" : getIssueUrl());
		properties.put("type", getType() == null ? "" : getType());
		properties.put("title", getTitle() == null ? "" : getTitle());
		properties.put("status", getStatus() == null ? "" : getStatus());
		properties.put("resolution", getResolution() == null ? "" : getResolution());
		properties.put("priority", getPriority() == null ? "" : getPriority());
		properties.put("commitLinkUrl", getCommitLinkUrl() == null ? "" : getCommitLinkUrl());
		properties.put("commitLinkId", getCommitLinkId() == null ? "" : getCommitLinkId());
		properties.put("createTime", getCreateTime() == null ? "" : getCreateTime());
		properties.put("updateTime", getUpdateTime() == null ? "" : getUpdateTime());
		properties.put("closeTime", getCloseTime() == null ? "" : getCloseTime());
		properties.put("reporter", getReporter() == null ? "" : getReporter());
		properties.put("issueLinks", getIssueLinks() == null ? "" : getIssueLinks());
		properties.put("description", getDescription() == null ? "" : getDescription());
		return properties;
	}

	@Override
	public NodeLabelType getNodeType() {
		return NodeLabelType.Issue;
	}
	
	@Override
	public String getName() {
		return title;
	}


}
