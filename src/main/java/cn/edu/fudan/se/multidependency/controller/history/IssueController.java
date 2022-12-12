package cn.edu.fudan.se.multidependency.controller.history;

import javax.servlet.http.HttpServletRequest;

import cn.edu.fudan.se.multidependency.model.node.Project;
import cn.edu.fudan.se.multidependency.service.query.structure.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import cn.edu.fudan.se.multidependency.model.node.git.Issue;
import cn.edu.fudan.se.multidependency.service.query.history.IssueQueryService;

@Controller
@RequestMapping("/issue")
public class IssueController {
	
	@Autowired
	private IssueQueryService issueService;

	@GetMapping("")
	public String index(HttpServletRequest request) {
		request.setAttribute("issues", issueService.queryIssueAddressedByCommit());
		return "history/issues";
	}
	
	@GetMapping("/files")
	@ResponseBody
	public Object relatedFiles() {
		return issueService.queryRelatedFilesOnAllIssues();
	}
	
	@GetMapping("/{issueId}")
	public String issue(HttpServletRequest request, @PathVariable("issueId") long issueId) {
		request.setAttribute("issue", issueService.queryIssue(issueId));
		return "history/issue";
	}

	@GetMapping("/gitRepo/{gitRepoId}")
	public String getIssuesByGitRepoId(HttpServletRequest request, @PathVariable("gitRepoId") long gitRepoId) {
		request.setAttribute("issues", issueService.queryIssuesByGitRepoId(gitRepoId));
		return "history/issues";
	}
	
	@GetMapping("/{issueId}/files")
	@ResponseBody
	public Object relatedFiles(@PathVariable("issueId") long issueId) {
		Issue issue = issueService.queryIssue(issueId);
		return issueService.queryRelatedFilesOnIssue(issue);
	}
	
	@GetMapping("/{issueId}/commits")
	@ResponseBody
	public Object relatedCommits(@PathVariable("issueId") long issueId) {
		Issue issue = issueService.queryIssue(issueId);
		return issueService.queryRelatedCommitsOnIssue(issue);
	}
	
}
