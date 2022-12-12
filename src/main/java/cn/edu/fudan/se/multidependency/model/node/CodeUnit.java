package cn.edu.fudan.se.multidependency.model.node;

import org.neo4j.ogm.annotation.NodeEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NodeEntity
@EqualsAndHashCode
@NoArgsConstructor
public abstract class CodeUnit implements CodeNode {
	
	private static final long serialVersionUID = -652895885078693272L;

}
