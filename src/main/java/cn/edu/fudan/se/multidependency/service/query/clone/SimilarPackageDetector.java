package cn.edu.fudan.se.multidependency.service.query.clone;

import java.io.OutputStream;
import java.util.Collection;

import cn.edu.fudan.se.multidependency.model.node.Node;
import cn.edu.fudan.se.multidependency.model.node.clone.CloneGroup;
import cn.edu.fudan.se.multidependency.service.query.clone.data.SimilarPackage;

public interface SimilarPackageDetector {

	Collection<SimilarPackage> detectSimilarPackages(int threshold, double percentage);

	void exportSimilarPackages(OutputStream stream);
}
