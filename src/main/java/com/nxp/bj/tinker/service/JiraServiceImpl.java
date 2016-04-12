package com.nxp.bj.tinker.service;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.net.URLEncoder.encode;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequest;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.util.Page;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.interfaces.JiraService;

public class JiraServiceImpl implements JiraService {

    private static final Logger logger = LoggerFactory.getLogger(JiraServiceImpl.class);
    
    private final ApplicationLinkService applicationLinkService;
    private final EntityLinkService entityLinkService;
    
    private final String inReviewToResolveID = "151";    // 151: jira test server from InReview to Resolve Issue  TODO 21 is the real id
    private final String resolveToOpen = "211";
    
    
    public JiraServiceImpl(
            ApplicationLinkService applicationLinkService,
            EntityLinkService entityLinkService) {
        this.applicationLinkService = applicationLinkService;
        this.entityLinkService = entityLinkService;
    }

    @Override
    public boolean updateIssueDescriptionWithSHAs(String issueKey, Page<? extends Changeset> pages, String whichRef) throws UnsupportedEncodingException, CredentialsRequiredException {
        checkNotNull(issueKey, "IssueKey is null");
        checkNotNull(pages, "Pages is null");
        
        try {
            ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.PUT, Constants.REST_API_2_ISSUE + encode(issueKey, UTF_8.name()));
            
            JSONObject issueResult = getIssueInfo(issueKey);
            
            Object descObj = null;
            String desc = "";
            if (issueResult != null) {
                JSONObject jsonFieldsObj = issueResult.getJSONObject(Constants.REST_API_KEY_OF_FIELDS);
                if (jsonFieldsObj != null) {
                    descObj = jsonFieldsObj.get(Constants.REST_API_KEY_OF_DESCRIPTION);
                    if (!descObj.equals(JSONObject.NULL)) {
                        desc = (String) descObj;
                    }
                }
            }

            Date now = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            
            StringBuilder sb = new StringBuilder(desc);
            sb.append("\n").append(Constants.MARK_OF_SHAS).append(" from ").append(whichRef);
            
            Iterator<? extends Changeset> iter = pages.getValues().iterator();
            while(iter.hasNext()) {
                Changeset cs = iter.next();
                sb.append(cs.getId()).append("\n");
            }

            sb.append(Constants.MARK_OF_AUTO_GENE).append(ft.format(now)).append("\n");

            JSONObject descSetEntity = new JSONObject();
            descSetEntity.put("set", sb.toString());

            JSONArray descSetArray = new JSONArray();
            descSetArray.put(descSetEntity);

            JSONObject descEntity = new JSONObject();
            descEntity.put("description", descSetArray);

            JSONObject rootEntity = new JSONObject();
            rootEntity.put("update", descEntity);

            appLinkRequest.setHeader("Content-Type", "application/json");
            appLinkRequest.setEntity(rootEntity.toString());

            appLinkRequest.execute();
            return true;
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateAssigneeAsIPOwner(String issueKey, String userId) throws UnsupportedEncodingException, CredentialsRequiredException {
        checkNotNull(userId, "UserId is null");
        
        try {
            ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                    .createRequest(Request.MethodType.PUT, Constants.REST_API_2_ISSUE
                                                            + encode(issueKey, UTF_8.name())
                                                            + "/assignee");

            appLinkRequest.setHeader("Content-Type", "application/json");
            appLinkRequest.setEntity("{\"name\":\"" + encode(userId, UTF_8.name()) + "\"}");
            appLinkRequest.execute();

            return true;
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        
        return false;
    }

    @Override
    public void transitionIssueStatusFromSolvedToIntegrated(String issueKey, String ipOwner, String branchName) throws UnsupportedEncodingException, CredentialsRequiredException {
        checkNotNull(issueKey, "issueKey is null");
        
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.POST, Constants.REST_API_2_ISSUE
                                                        + encode(issueKey, UTF_8.name()) 
                                                        + "/transitions?expand=transitions.fields");

        appLinkRequest.setHeader("Content-Type", "application/json");

        JSONObject transitionIdEntity = new JSONObject();
        transitionIdEntity.put("id", "191");    // {"id":"81","name":"Integrate"} in sw-jira. Test server is 191 TODO

        JSONObject fieldsEntity = new JSONObject();
        fieldsEntity.put("customfield_10100", new JSONObject().put("name", ipOwner));   // customfield_11524 in sw-jira TODO
        
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        fieldsEntity.put("customfield_10101", ft.format(new Date()));   // customfield_11522 in sw-jira     TODO
        
        fieldsEntity.put("customfield_10200", branchName);  // customfield_11528 in sw-jira        TODO
        
        JSONObject rootEntity = new JSONObject();
        rootEntity.put("transition", transitionIdEntity);

        appLinkRequest.setEntity(rootEntity.toString());

        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transitionIssueStatusFromInReviewToSolveIssue(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException {
        checkNotNull(issueKey, "issueKey is null");
        
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.POST, Constants.REST_API_2_ISSUE
                                                        + encode(issueKey, UTF_8.name()) 
                                                        + "/transitions?expand=transitions.fields");
        
        appLinkRequest.setHeader("Content-Type", "application/json");
        
        JSONObject rootEntity = resolveIssueEntity();
        
        appLinkRequest.setEntity(rootEntity.toString());
        
        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void transitionIssueFromInReviewToOpen(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException {
        checkNotNull(issueKey, "issueKey is null");
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.POST, Constants.REST_API_2_ISSUE
                                                        + encode(issueKey, UTF_8.name()) 
                                                        + "/transitions?expand=transitions.fields");
        appLinkRequest.setHeader("Content-Type", "application/json");
        
        JSONObject rootEntity = resolveIssueEntity();
        
        appLinkRequest.setEntity(rootEntity.toString());

        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        JSONObject rootEntityReOpen = new JSONObject();
        JSONObject transitionIdEntityReOpen = new JSONObject();
        transitionIdEntityReOpen.put("id", this.resolveToOpen);
        rootEntityReOpen.put("transition", transitionIdEntityReOpen);

        appLinkRequest.setEntity(rootEntityReOpen.toString());
        
        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
    }
    

    
    @Override
    public void transitionIssueStatusFromOpenToInReview(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException {
        checkNotNull(issueKey, "issueKey is null");
        
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.POST, Constants.REST_API_2_ISSUE
                                                        + encode(issueKey, UTF_8.name()) 
                                                        + "/transitions?expand=transitions.fields");

        appLinkRequest.setHeader("Content-Type", "application/json");
        
        // {"id":"11","name":"Start Progress"} issue start progress first
        appLinkRequest.setEntity("{\"transition\" : {\"id\":\"11\"}}");
        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        // {"id":"71","name":"Ready for Review"} test is 21 TODO
        appLinkRequest.setEntity("{\"transition\" : {\"id\":\"21\"}}");

        try {
            appLinkRequest.execute();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public JSONObject getIssueInfo(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException {
        checkNotNull(issueKey, "issueKey is null");
        
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.GET, "/rest/api/2/search/?jql=" + encode("issueKey="+issueKey, UTF_8.name()));

        String queryResult = "";

        try {
            queryResult = appLinkRequest.execute();
            JSONObject resultObj = new JSONObject(queryResult);
            
            int issueTotal = resultObj.getInt(Constants.REST_API_KEY_OF_TOTAL);
            if (issueTotal == 0) {
                return null;
            } else {
                return resultObj.getJSONArray(Constants.REST_API_KEY_OF_ISSUES).getJSONObject(0);
            }
            
        } catch (ResponseException e) {
            if (e.getMessage().contains("Status code: 400")) {
                return null;
            }
        }

        return null;
    }

    @Override
    public boolean isIssueStatusOpen(String issueKey) throws CredentialsRequiredException, UnsupportedEncodingException {
        checkNotNull(issueKey, "issueKey is null");
        
        ApplicationLinkRequest appLinkRequest = getJiraApplicationLink().createAuthenticatedRequestFactory()
                .createRequest(Request.MethodType.GET, "/rest/api/2/search/?jql=" + encode("issueKey="+issueKey, UTF_8.name()));

        String queryResult = "";

        try {
            queryResult = appLinkRequest.execute();
        } catch (ResponseException e) {
            if (e.getMessage().contains("Status code: 400")) {
                return false;
            }
        }

        JSONObject resultObj = new JSONObject(queryResult);
        int issueTotal = resultObj.getInt(Constants.REST_API_KEY_OF_TOTAL);
        if (issueTotal == 0) {
            return false;
        } else {
            String status = resultObj.getJSONArray(Constants.REST_API_KEY_OF_ISSUES).getJSONObject(0)
                    .getJSONObject(Constants.REST_API_KEY_OF_FIELDS)
                    .getJSONObject(Constants.REST_API_KEY_OF_STATUS)
                    .getString(Constants.REST_API_KEY_OF_NAME);

            if (Constants.ISSUE_STATUS_OPEN.equalsIgnoreCase(status)) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Get Jira application link
     * @return
     */
    private ApplicationLink getJiraApplicationLink() {
        ApplicationLink applicationLink = applicationLinkService.getPrimaryApplicationLink(JiraApplicationType.class);

        if (applicationLink == null) {
            throw new IllegalStateException("Primary JIRA application link does not exist!");
        }

        return applicationLink;
    }
    
    private JSONObject resolveIssueEntity() {
        JSONObject transitionIdEntity = new JSONObject();
        transitionIdEntity.put("id", this.inReviewToResolveID);    // 151: jira test server from InReview to Resolve Issue  TODO 21 is the real id

        JSONObject fieldsEntity = new JSONObject();
        fieldsEntity.put("resolution", new JSONObject().put("name", "Won't Fix"));  // Won't Fix in sw-jira
        fieldsEntity.put("customfield_10000", "N/A");   // customfield_10401 in sw-jira

        // TODO start
        // Fix Version/s
        fieldsEntity.put("fixVersions", new JSONArray().put(new JSONObject().put("name", "QLINUX SDK V2-0")));

        // Root Cause (cascading)
        JSONObject child = new JSONObject();
        child.put("value", "N/A");
        
        JSONObject father = new JSONObject();
        father.put("value", "OTHER");
        father.put("child", child);
        fieldsEntity.put("customfield_10016", father);

        // Phase Originated
        fieldsEntity.put("customfield_10003", new JSONObject().put("value", "External"));
        
        // Klocwork Project
        fieldsEntity.put("customfield_10004", "N/A");
        
        // Klocwork Scope
        fieldsEntity.put("customfield_10005", "N/A");
        
        // Klocwork Build
        fieldsEntity.put("customfield_10006", "N/A");
        
        // Upstream Report
        fieldsEntity.put("customfield_10011", "N/A");
        
        // Unit Test Report 
        fieldsEntity.put("customfield_10012", "N/A");
        
        // Legal Review Status
        fieldsEntity.put("customfield_10013", new JSONObject().put("value", "In Review"));
        
        // Documentation Status
        fieldsEntity.put("customfield_10014", "N/A");
        // TODO remove this

        JSONObject rootEntity = new JSONObject();
        rootEntity.put("transition", transitionIdEntity);
        rootEntity.put("fields", fieldsEntity);
        
        return rootEntity;
    }
}
