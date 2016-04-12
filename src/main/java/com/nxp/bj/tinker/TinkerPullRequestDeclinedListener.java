package com.nxp.bj.tinker;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.event.api.EventListener;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.event.pull.PullRequestDeclinedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.JiraServiceImpl;
import com.nxp.bj.tinker.service.interfaces.JiraService;
import com.nxp.bj.tinker.utils.TinkerUtils;

public class TinkerPullRequestDeclinedListener {
    private static final Logger logger = LoggerFactory.getLogger(TinkerPullRequestDeclinedListener.class);
    
    private final PluginSettings pluginSettings;
    
    private JiraService jiraService;
    
    public TinkerPullRequestDeclinedListener(
            PluginSettingsFactory pluginSettingsFactory,
            ApplicationLinkService applicationLinkService,
            EntityLinkService entityLinkService) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        
        jiraService = new JiraServiceImpl(applicationLinkService, entityLinkService);
    }
    
    @EventListener
    public void onPullRequestDeclinedEvent(PullRequestDeclinedEvent event) {
        PullRequest pullRequest = event.getPullRequest();
        
        if(!TinkerUtils.isProjReposMapped(pullRequest, pluginSettings)) {
            return;
        }
        
        Map<String, String> fromRefBranchInfo = TinkerUtils.parseFromRefBranchInfo(pullRequest);
        
        String fromRefBranchPrefix = fromRefBranchInfo.get(Constants.KEY_OF_REF_PREFIX);
        
        
        if (TinkerUtils.isJiraBranch(fromRefBranchPrefix)) {
            String ticket = fromRefBranchInfo.get(Constants.KEY_OF_REF_JIRA_TICKET);
            
            try {
                jiraService.transitionIssueFromInReviewToOpen(ticket);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (CredentialsRequiredException e) {
                e.printStackTrace();
            }
        }
    }
}
