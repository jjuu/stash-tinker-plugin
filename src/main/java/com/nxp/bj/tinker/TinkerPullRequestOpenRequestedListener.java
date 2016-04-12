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
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.event.pull.PullRequestOpenRequestedEvent;
import com.atlassian.stash.i18n.I18nService;
import com.atlassian.stash.pull.PullRequest;
import com.atlassian.stash.user.PermissionValidationService;
import com.atlassian.stash.user.SecurityService;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.JiraServiceImpl;
import com.nxp.bj.tinker.service.interfaces.JiraService;
import com.nxp.bj.tinker.utils.TinkerUtils;;


public class TinkerPullRequestOpenRequestedListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TinkerPullRequestOpenRequestedListener.class);
    
    private final I18nService i18nService;
    private final SecurityService securityService;
    private final PluginSettings pluginSettings;
    private final PermissionValidationService permissionValidationService;
    private final ApplicationLinkService applicationLinkService;
    private final EntityLinkService entityLinkService;
    
    private JiraService jiraService;
    
    public TinkerPullRequestOpenRequestedListener(
            PermissionValidationService permissionValidationService,
            ApplicationLinkService applicationLinkService,
            PluginSettingsFactory pluginSettingsFactory,
            EntityLinkService entityLinkService,
            SecurityService securityService,
            I18nService i18nService) {
        this.permissionValidationService = permissionValidationService;
        this.applicationLinkService = applicationLinkService;
        this.securityService = securityService;
        this.i18nService = i18nService;
        this.entityLinkService = entityLinkService;
        
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
        
        jiraService = new JiraServiceImpl(applicationLinkService, entityLinkService);
    }

    @EventListener
    public void onPullRequestOpenRequestedEvent(PullRequestOpenRequestedEvent event) {
        PullRequest pullRequest = event.getPullRequest();

        if(!TinkerUtils.isProjReposMapped(pullRequest, pluginSettings)) {
            return;
        }

        try {
            Map<String, String> fromRefBranchInfo = TinkerUtils.parseFromRefBranchInfo(pullRequest);
            Map<String, String> toRefBranchInfo = TinkerUtils.parseToRefBranchInfo(pullRequest);

            String fromRefBranchPrefix = fromRefBranchInfo.get(Constants.KEY_OF_REF_PREFIX);

            // TODO prefix name "jira" should be configurable
            if (TinkerUtils.isJiraBranch(fromRefBranchPrefix)) {
                String toRefBranchPrefix = toRefBranchInfo.get(Constants.KEY_OF_REF_PREFIX);

                String ticket = fromRefBranchInfo.get(Constants.KEY_OF_REF_JIRA_TICKET);
                
                String fromRefFeatureName = "";
                if (fromRefBranchInfo.containsKey(Constants.KEY_OF_REF_FEATURE_NAME)) {
                    fromRefFeatureName = fromRefBranchInfo.get(Constants.KEY_OF_REF_FEATURE_NAME);
                }

                String toRefFeatureName = "";
                if (toRefBranchInfo.containsKey(Constants.KEY_OF_REF_FEATURE_NAME)) {
                    toRefFeatureName = toRefBranchInfo.get(Constants.KEY_OF_REF_FEATURE_NAME);
                }
                
                if (fromRefBranchInfo.size() != 3) {
                    event.cancel(i18nService.createKeyedMessage("tinker.pullrequest.fromref.invalid.format"));
                } else if (toRefBranchInfo.size() != 2) {
                    event.cancel(i18nService.createKeyedMessage("tinker.pullrequest.toref.invalid.format"));
                } else if (!Constants.REF_PREFIX_TO_BRANCH.equals(toRefBranchPrefix)) {
                    event.cancel(i18nService.createKeyedMessage("tinker.pullrequest.toref.startswith.feature"));
                } else if (!fromRefFeatureName.equals(toRefFeatureName)) {
                    event.cancel(i18nService.createKeyedMessage("tinker.pullrequest.unequal.feature.name"));
                }
                
                boolean isIssueOpen = jiraService.isIssueStatusOpen(ticket);
                
                if (!isIssueOpen) {
                    event.cancel(i18nService.createKeyedMessage("tinker.pullrequest.issue.status.not.open"));
                }
                
            }
        } catch (CredentialsRequiredException e) {
            logger.error(e.toString());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            logger.error(e.toString());
            e.printStackTrace();
        }

    }
}
