package com.nxp.bj.tinker;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.utils.TinkerUtils;

import static com.google.common.base.Preconditions.checkNotNull;

public class TinkerRemoveProjReposItemServlet extends HttpServlet {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(TinkerRemoveProjReposItemServlet.class);
    
    private final PluginSettings pluginSettings;
    
    public TinkerRemoveProjReposItemServlet(
            PluginSettingsFactory pluginSettingsFactory) {
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }
    
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String projKey = req.getParameter(Constants.KEY_OF_PROJECT_KEY).trim();
        String reposName = req.getParameter(Constants.KEY_OF_REPOSITORY_NAME).trim();

        @SuppressWarnings("unchecked")
        Map<String, Object> projReposInfoMap = (Map<String, Object>) pluginSettings.get(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE);
        if (projReposInfoMap.containsKey(projKey)) {
            String reposNameListStr = (String)projReposInfoMap.get(projKey);
            List<String> reposNameTempList = TinkerUtils.convertReposArrayToList(reposNameListStr);
            
            reposNameTempList.remove(reposName);
            
            projReposInfoMap.put(projKey, reposNameTempList);
            
            pluginSettings.put(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE, projReposInfoMap);
            
            resp.sendRedirect("tinkerConfig");
        }
        
    }
}
