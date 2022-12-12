package cn.edu.fudan.se.multidependency.service.query;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Developer;
import cn.edu.fudan.se.multidependency.repository.node.ProjectFileRepository;
import cn.edu.fudan.se.multidependency.repository.node.git.DeveloperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CoDeveloperServiceImpl implements CoDeveloperService{

    @Autowired
    DeveloperRepository developerRepository;

    @Autowired
    ProjectFileRepository projectFileRepository;


    @Override
    public Map<String, List<ProjectFile>> calAllFilesAndCreator() {
        Map<String, List<ProjectFile>> creatorsAndFiles = new HashMap<>();
        List<String> developers = getAllDeveloperName();
        for (String name:
             developers) {
            List<ProjectFile> files = projectFileRepository.findFilesByCreatorName(name);
            if (files.size() > 0) creatorsAndFiles.put(name, files);
        }
        return creatorsAndFiles;
    }

    @Override
    public Map<String, List<ProjectFile>> calAllFilesAndLastUpdator() {
        Map<String, List<ProjectFile>> creatorsAndFiles = new HashMap<>();
        List<String> developers = getAllDeveloperName();
        for (String name:
                developers) {
            List<ProjectFile> files = projectFileRepository.findFilesByLastUpdatorName(name);
            if (files.size() > 0) creatorsAndFiles.put(name, files);
        }
        return creatorsAndFiles;
    }

    @Override
    public Map<String, List<ProjectFile>> calAllFilesAndMostUpdator() {
        Map<String, List<ProjectFile>> creatorsAndFiles = new HashMap<>();
        List<String> developers = getAllDeveloperName();
        for (String name:
                developers) {
            List<ProjectFile> files = projectFileRepository.findFilesByMostUpdatorName(name);
            if (files.size() > 0) creatorsAndFiles.put(name, files);
        }
        return creatorsAndFiles;
    }

    private List<String> getAllDeveloperName(){
        List<String> developers = developerRepository.queryAllDevelopers().stream()
                .map(Developer::getName).collect(Collectors.toList());
        return developers;
    }

    @Override
    public List<ProjectFile> findCoCreatorFileList(Long fileId) {
        return projectFileRepository.findCoCreatorFileList(fileId);
    }

    @Override
    public List<ProjectFile> findCoLastUpdatorFileList(Long fileId) {
        return projectFileRepository.findCoLastUpdatorFileList(fileId);
    }

    @Override
    public List<ProjectFile> findCoMostUpdatorFileList(Long fileId) {
        return projectFileRepository.findCoMostUpdatorFileList(fileId);
    }
}
