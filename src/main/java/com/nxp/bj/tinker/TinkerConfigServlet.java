package com.nxp.bj.tinker;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.auth.LoginUriProvider;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.soy.renderer.SoyException;
import com.atlassian.soy.renderer.SoyTemplateRenderer;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.setting.SettingsValidationErrors;
import com.atlassian.webresource.api.assembler.PageBuilderService;
import com.google.common.collect.ImmutableMap;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.constants.TinkerError;
import com.nxp.bj.tinker.service.StashServiceImpl;
import com.nxp.bj.tinker.utils.TinkerUtils;

public class TinkerConfigServlet extends HttpServlet {

    private final Logger logger = LoggerFactory.getLogger(TinkerConfigServlet.class);
    
    private final RepositoryHookService repositoryHookService;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;
    private final PluginSettings pluginSettings;
    private final LoginUriProvider loginUriProvider;
    private final PageBuilderService pageBuilderService;
    private final StashServiceImpl tinkerStashService;
    
    private Map<String, Object> fields;
    private Map<String, Object> settingsMap;
    private Map<String, Iterable<String>> fieldErrors;
    
    private final SoyTemplateRenderer soyTemplateRenderer;
    
    public TinkerConfigServlet(
            UserManager userManager,
            LoginUriProvider loginUriProvider,
            SoyTemplateRenderer soyTemplateRenderer,
            PluginSettingsFactory pluginSettingsFactory,
            RepositoryHookService repositoryHookService,
            ApplicationProperties applicationProperties,
            PageBuilderService pageBuilderService,
            StashServiceImpl tinkerStashService) {
        this.userManager = userManager;
        this.loginUriProvider = loginUriProvider;
        this.soyTemplateRenderer = soyTemplateRenderer;
        this.repositoryHookService = repositoryHookService;
        this.applicationProperties = applicationProperties;
        this.pageBuilderService = pageBuilderService;
        this.tinkerStashService = tinkerStashService;

        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();

        fields = new HashMap<String, Object>();
        fieldErrors = new HashMap<String, Iterable<String>>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        /*String username = userManager.getRemoteUsername(request);
        if (username == null || !userManager.isSystemAdmin(username)) {
            redirectToLogin(request, resp);
            return;
        }*/

//        settingsMap = (Map<String, Object>) pluginSettings.get(Constants.KEY_OF_TINKER_PLUGIN_STORAGE);

//        pluginSettings.put(Constants.KEY_OF_TINKER_PLUGIN_STORAGE, settingsMap);

        // Init the project field
        Map<String, String> projectInfo = tinkerStashService.retrieveAllCurrUserCanSeeProjectInfo();
        fields.put(Constants.KEY_OF_PROJECT_INFO, projectInfo);

        // Init the repositories of the frist project
        List<Repository> initReposList = new ArrayList<Repository>();
        List<String> reposNameList = new ArrayList<String>();

        if (projectInfo != null && !projectInfo.isEmpty()) {
            for (Map.Entry<String, String> projEntry : projectInfo.entrySet()) {
                initReposList = tinkerStashService.retrieveRepositoriesByProjectKey(projEntry.getKey());
                for (Repository repos : initReposList) {
                    reposNameList.add(repos.getName());
                }
                break;
            }
        }

        // init the repository field
        fields.put(Constants.KEY_OF_INIT_REPOS, reposNameList);

        // Bind web resource to the plugin
        pageBuilderService.assembler().resources().requireWebResource("nxp.bj.nxp-workflow-tinker:tinker-resources");

        // encode by utf-8
        resp.setContentType("text/html;charset=utf-8");

        Map<String, Object> projReposInfoMap = retrieveProjReposInfo();
        
        Map<String, List<String>> projReposInfo = new HashMap<String, List<String>>();
        for(Map.Entry<String, Object> projReposEntry : projReposInfoMap.entrySet()) {
            String projKey = projReposEntry.getKey();
            String reposNameStr = (String) projReposEntry.getValue();
            List<String> reposNameTempList = TinkerUtils.convertReposArrayToList(reposNameStr);
            projReposInfo.put(projKey, reposNameTempList);
        }
        
        try {
            soyTemplateRenderer.render(
                resp.getWriter(),
                PluginMetadata.getCompleteModuleKey("soy-templates"),
                "stash.templates.tinker.settings.workflowTinkerSettings",
                ImmutableMap
                    .<String, Object>builder()
                    .put(Constants.KEY_OF_STORAGE_NAME, fields)
                    .put(Constants.KEY_OF_PROJECT_REPOSITORY_MAP, projReposInfo)
                    .put(Constants.KEY_OF_TINKER_PLUGIN_FIELD_ERROR, fieldErrors)
                    .build()
            );
        } catch (SoyException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new ServletException(e);
        }
        
        fieldErrors.clear();
    }


    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

//        settingsMap = (Map<String, Object>) pluginSettings.get(Constants.KEY_OF_TINKER_PLUGIN_STORAGE);

        String projectKey = req.getParameter(Constants.KEY_OF_PROJECT_NAME).trim();
        String repositoryName = req.getParameter(Constants.KEY_OF_REPOSITORY_NAME).trim();

        // do validate check
        validateAndParseSettings(projectKey, repositoryName);

        response.sendRedirect("tinkerConfig");
    }


    @SuppressWarnings("unchecked")
    private Map<String, Object> retrieveProjReposInfo() {
        Map<String, Object> projReposInfo = (Map<String, Object>) pluginSettings.get(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE);
        if (projReposInfo == null ) {
            projReposInfo = new LinkedHashMap<String, Object>();
            pluginSettings.put(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE, projReposInfo);
        }
        
        return projReposInfo;
    }


    private void redirectToLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(loginUriProvider.getLoginUri(getUri(request)).toASCIIString());
    }


    private URI getUri(HttpServletRequest request) {
        StringBuffer builder = request.getRequestURL();
        if (request.getQueryString() != null) {
            builder.append("?");
            builder.append(request.getQueryString());
        }
        
        return URI.create(builder.toString());
    }

    /*private List<String> convertReposArrayToList(String reposArrayString) {
        String[] reposNameArr = reposArrayString.replace("[", "").replace("]", "").split(",");
        
        List<String> reposNameList = new ArrayList<String>();
        for (int index = 0 ; index < reposNameArr.length ; index++) {
            reposNameList.add(reposNameArr[index].trim());
        }
        
        return reposNameList;
    }*/

    @SuppressWarnings("unchecked")
    private void validateAndParseSettings(String projectKey, String repositoryName) {
        SettingsValidationErrors errors = new SettingsValidationErrorsImpl(fieldErrors);
        Map<String, Object> projReposInfo = retrieveProjReposInfo();
        
        List<String> reposNameList = null;
        if (projReposInfo.containsKey(projectKey)) {
            String reposNameListStr = (String)projReposInfo.get(projectKey);
            reposNameList = TinkerUtils.convertReposArrayToList(reposNameListStr);

            if (reposNameList.contains(repositoryName)) {
                errors.addFieldError(Constants.KEY_OF_EXIST_ERROR,
                        new StringBuffer(TinkerError.RESPOSITORY_EXIST_ERROR_MSG).append(" (").append(repositoryName).append(")").toString());
                return;
            }
        } else {
            reposNameList = new ArrayList<String>();
        }
        
        reposNameList.add(repositoryName);
        projReposInfo.put(projectKey, reposNameList);
        
        pluginSettings.put(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE, projReposInfo);
    }

    private static class SettingsValidationErrorsImpl implements SettingsValidationErrors {
        Map<String, Iterable<String>> fieldErrors;
        
        public SettingsValidationErrorsImpl(Map<String, Iterable<String>> fieldErrors) {
            this.fieldErrors = fieldErrors;
            this.fieldErrors.clear();
        }
        
        @Override
        public void addFieldError(String fieldName, String errorMessage) {
            fieldErrors.put(fieldName, new ArrayList<String>(Collections.singletonList(errorMessage)));
        }

        @Override
        public void addFormError(String arg0) {
          //not implemented
        }

        public boolean containsError(String errorKey) {
            if (this.fieldErrors.containsKey(errorKey)) {
                return true;
            } else {
                return false;
            }
        }
    }
}
