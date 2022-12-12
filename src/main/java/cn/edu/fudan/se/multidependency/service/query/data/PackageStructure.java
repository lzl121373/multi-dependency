package cn.edu.fudan.se.multidependency.service.query.data;

import cn.edu.fudan.se.multidependency.model.node.Package;
import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.service.query.structure.ContainRelationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PackageStructure {

    @Autowired
    ContainRelationService containRelationService;

    @Setter
    @Getter
    private Package pck;

    @Setter
    @Getter
    private List<PackageStructure> childrenPackages = new ArrayList<>();

    @Setter
    @Getter
    private List<ProjectFile> childrenFiles = new ArrayList<>();

    private PackageStructure packageFind = null;

    public PackageStructure(Package pck) {
        this.pck = pck;
    }

    public boolean isEmptyChildrenPackages(){
        return childrenPackages.isEmpty();
    }

//    public PackageStructure getChildPackage(Package pck){
//        packageStructureRecursion(childrenPackages,pck);
//        return packageFind;
//    }
//
//    private void packageStructureRecursion(List<PackageStructure> children,Package pck){
//        for(PackageStructure pck2:children){
//            if(pck2.getPck().getId() == pck.getId()){
//                packageFind = pck2;
//            }else{
//                List<PackageStructure> childrenPackages2 = pck2.getChildrenPackages();
//                packageStructureRecursion(childrenPackages2,pck);
//            }
//        }
//    }

    public void addAllFiles(Collection<ProjectFile> files) {
        childrenFiles.addAll(files);
    }

    public void addChildPackage(PackageStructure child) {
        this.childrenPackages.add(child);
    }

}
