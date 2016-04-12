package com.nxp.bj.tinker;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.EntityLinkService;
import com.atlassian.event.api.EventListener;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.event.pull.PullRequestMergedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.repository.Repository;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.JiraServiceImpl;
import com.nxp.bj.tinker.service.interfaces.JiraService;
import com.nxp.bj.tinker.utils.TinkerUtils;

public class TinkerPullRequestMergedListener {
    private static final Logger logger = LoggerFactory.getLogger(TinkerPullRequestMergedListener.class);
    
    private final PluginSettings pluginSettings;
    private final EntityLinkService entityLinkService;
    private final ApplicationLinkService applicationLinkService;
    
    private JiraService jiraService;
    
    public TinkerPullRequestMergedListener(
            ApplicationLinkService applicationLinkService,
            PluginSettingsFactory pluginSettingsFactory,
            EntityLinkService entityLinkService) {
        
        this.entityLinkService = entityLinkService;
        this.applicationLinkService = applicationLinkService;
        
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        
        jiraService = new JiraServiceImpl(applicationLinkService, entityLinkService);
    }
    
    @EventListener
    public void onPullRequestMerged(PullRequestMergedEvent event) {
        PullRequest pullRequest = event.getPullRequest();
        if(!TinkerUtils.isProjReposMapped(pullRequest, pluginSettings)) {
            return;
        }

        Map<String, String> fromRefBranchInfo = TinkerUtils.parseFromRefBranchInfo(pullRequest);
        Map<String, String> toRefBranchInfo = TinkerUtils.parseToRefBranchInfo(pullRequest);

        String fromRefBranchPrefix = fromRefBranchInfo.get(Constants.KEY_OF_REF_PREFIX);
        
        Repository repos = event.getRepository();
        
        try {
            if (TinkerUtils.isJiraBranch(fromRefBranchPrefix)) {
                String ticket = fromRefBranchInfo.get(Constants.KEY_OF_REF_JIRA_TICKET);

                jiraService.transitionIssueStatusFromInReviewToSolveIssue(ticket);

                String toFeatureName = toRefBranchInfo.get(Constants.KEY_OF_REF_FEATURE_NAME);
                TinkerUtils.saveTickets(pluginSettings,
                        repos.getProject().getKey(),
                        repos.getName(),
                        toFeatureName,
                        ticket);
                
            } else if (Constants.REF_PREFIX_TO_BRANCH.equals(fromRefBranchPrefix)) {
                String fromRefFeatureName = fromRefBranchInfo.get(Constants.KEY_OF_REF_FEATURE_NAME);
                String ipOwner = TinkerUtils.getIPOwner(fromRefFeatureName);
                
                List<String> ticketList = TinkerUtils.getSavedTickets(pluginSettings, repos.getProject().getKey(), repos.getName(), fromRefFeatureName);
                for (String ticket : ticketList) {
                    jiraService.transitionIssueStatusFromSolvedToIntegrated(ticket, ipOwner, "feature/" + fromRefFeatureName);
                }

                TinkerUtils.removeStorageTickets(pluginSettings, repos.getProject().getKey(), repos.getName(), fromRefFeatureName);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (CredentialsRequiredException e) {
            e.printStackTrace();
        }
    }
}
