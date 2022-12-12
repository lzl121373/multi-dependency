package cn.edu.fudan.se.multidependency.service.query.clone.data;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
public class PackageCloneValueWithFileCoChangeMatrix {

    Map<String, FileCloneWithCoChange> fileClone;

    LinkedHashSet<ProjectFile> cloneFiles1;

    LinkedHashSet<ProjectFile> cloneFiles2;

    Set<ProjectFile> noneCloneFiles1;

    Set<ProjectFile> noneCloneFiles2;

    boolean[][] matrix;
}
