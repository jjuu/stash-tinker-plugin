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
import com.atlassian.stash.commit.CommitService;
import com.atlassian.stash.content.Changeset;
import com.atlassian.stash.content.ChangesetsBetweenRequest;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.JiraServiceImpl;
import com.nxp.bj.tinker.service.interfaces.JiraService;
import com.nxp.bj.tinker.utils.TinkerUtils;

public class TinkerPullRequestOpenedListener {
    private static final Logger logger = LoggerFactory.getLogger(TinkerPullRequestOpenedListener.class);

    private final PluginSettings pluginSettings;
    private CommitService commitService;
    private JiraService jiraService;
    
    public TinkerPullRequestOpenedListener(
            ApplicationLinkService applicationLinkService,
            PluginSettingsFactory pluginSettingsFactory,
            EntityLinkService entityLinkService,
            CommitService commitService) {
        
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        this.commitService = commitService;
        jiraService = new JiraServiceImpl(applicationLinkService, entityLinkService);
    }

    @EventListener
    public void onPullRequestOpenedEvent(PullRequestOpenedEvent event) {
        PullRequest pullRequest = event.getPullRequest();
        if(!TinkerUtils.isProjReposMapped(pullRequest, pluginSettings)) {
            return;
        }

        Map<String, String> fromRefBranchInfo = TinkerUtils.parseFromRefBranchInfo(pullRequest);
        
        String fromRefBranchPrefix = fromRefBranchInfo.get(Constants.KEY_OF_REF_PREFIX);
        String fromRefBranchFeatureName = fromRefBranchInfo.get(Constants.KEY_OF_REF_FEATURE_NAME);
        
        if (TinkerUtils.isJiraBranch(fromRefBranchPrefix)) {
            String ticket = fromRefBranchInfo.get(Constants.KEY_OF_REF_JIRA_TICKET);

            Page<? extends Changeset> pages = commitService.getChangesetsBetween(
                    new ChangesetsBetweenRequest.Builder(pullRequest).build(),
                    new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT));

            try {
                // mark the ticket as "in review"
                jiraService.transitionIssueStatusFromOpenToInReview(ticket);
                
                // Assige the owner to the IP Owner
                String ipOwner = TinkerUtils.getIPOwner(fromRefBranchFeatureName);
                jiraService.updateAssigneeAsIPOwner(ticket, ipOwner);
                
                // Update the description with SHA(s)
                String fromRefBranch = pullRequest.getFromRef().getDisplayId();
                jiraService.updateIssueDescriptionWithSHAs(ticket, pages, fromRefBranch);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.toString());
                e.printStackTrace();
            } catch (CredentialsRequiredException e) {
                logger.error(e.toString());
                e.printStackTrace();
            }
        }
    }
}
