package com.nxp.bj.tinker.service.interfaces;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.json.JSONObject;

import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.util.Page;

public interface JiraService {
    public void transitionIssueFromInReviewToOpen(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException;
    public void transitionIssueStatusFromOpenToInReview(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException;
    public void transitionIssueStatusFromInReviewToSolveIssue(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException;
    public void transitionIssueStatusFromSolvedToIntegrated(String issueKey, String ipOwner, String branchName) throws CredentialsRequiredException, UnsupportedEncodingException;
    
    public boolean isIssueStatusOpen(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException;
    public boolean updateAssigneeAsIPOwner(String issueKey, String userId) throws CredentialsRequiredException, UnsupportedEncodingException;
    public boolean updateIssueDescriptionWithSHAs(String issueKey, Page<? extends Changeset> pages, String whichRef) throws CredentialsRequiredException, UnsupportedEncodingException;
    public JSONObject getIssueInfo(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException;
    
}
