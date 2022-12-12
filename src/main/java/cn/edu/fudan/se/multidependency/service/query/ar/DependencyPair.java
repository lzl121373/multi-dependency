package cn.edu.fudan.se.multidependency.service.query.ar;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.neo4j.annotation.QueryResult;

@Data
@QueryResult
public class DependencyPair {

    @Getter
    private ProjectFile projectFile;

    @Getter
    private int count;

}
