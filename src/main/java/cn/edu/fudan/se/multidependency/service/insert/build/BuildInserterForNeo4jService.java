package cn.edu.fudan.se.multidependency.service.insert.build;

import java.io.File;

import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;

public class BuildInserterForNeo4jService extends ExtractorForNodesAndRelationsImpl {

	protected File buildInfoFile;
	
	public File getBuildInfoFile() {
		return buildInfoFile;
	}

	public void setBuildInfoFile(File buildInfoFile) {
		this.buildInfoFile = buildInfoFile;
	}

	public void addNodesAndRelations() {
		/*try(BufferedReader reader = new BufferedReader(new FileReader(buildInfoFile))) {
			String line = null;
			//可以检查下，关系是否存在
			while((line = reader.readLine()) != null) {
				String[] fileNames = line.split(";");
				String targetPath = this.getNodes().getProject().getProjectPath() + "/" + fileNames[0];
				ProjectFile targetFile = this.getNodes().findCodeFileByPath(targetPath);
				if(targetFile == null) {
					targetFile = new ProjectFile(fileNames[0],targetPath,FileUtils.extractSuffix(fileNames[0]));
					targetFile.setEntityId(generateEntityId());
					addNode(targetFile);
				}
				for(int i = 1; i < fileNames.length; i++) {
					String sourcePath = this.getNodes().getProject().getProjectPath() + "/" + fileNames[i];
					ProjectFile sourceFile = this.getNodes().findCodeFileByPath(sourcePath);
					if(sourceFile == null) {
						sourceFile = new ProjectFile(fileNames[i],sourcePath,FileUtils.extractSuffix(fileNames[i]));
						sourceFile.setEntityId(generateEntityId());
						addNode(sourceFile);
					}
					FileBuildDependsFile relation = new FileBuildDependsFile(targetFile,sourceFile);
					addRelation(relation);
				}	
			}
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}*/
	}
	

	
}
