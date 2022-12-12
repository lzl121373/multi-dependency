package cn.edu.fudan.se.multidependency.service.query.history.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.edu.fudan.se.multidependency.model.node.ProjectFile;
import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class IssueFile {
	
	public IssueFile(@NonNull ProjectFile file) {
		this.file = file;
	}

	@Setter
	@Getter
	ProjectFile file;
	
	@Getter
	List<Issue> issues = new ArrayList<>();
	
	public void addAll(Collection<Issue> issues) {
		this.issues.addAll(issues);
	}
	
	public static IssueFile contains(List<IssueFile> issueFiles, ProjectFile file) {
		IssueFile temp = new IssueFile(file);
		int index = issueFiles.indexOf(temp);
		if(index < 0) {
			return null;
		}
		return issueFiles.get(index);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IssueFile other = (IssueFile) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		return true;
	}
	
}
