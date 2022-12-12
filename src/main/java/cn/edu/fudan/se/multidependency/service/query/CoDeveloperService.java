package cn.edu.fudan.se.multidependency.service.query;

import java.util.List;
import java.util.Map;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;

public interface CoDeveloperService {

    Map<String, List<ProjectFile>> calAllFilesAndCreator();

    Map<String, List<ProjectFile>> calAllFilesAndLastUpdator();

    Map<String, List<ProjectFile>> calAllFilesAndMostUpdator();

    List<ProjectFile> findCoCreatorFileList(Long fileId);

    List<ProjectFile> findCoLastUpdatorFileList(Long fileId);

    List<ProjectFile> findCoMostUpdatorFileList(Long fileId);
}
