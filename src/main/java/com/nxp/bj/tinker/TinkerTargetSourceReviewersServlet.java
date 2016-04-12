package com.nxp.bj.tinker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.stash.user.StashUser;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.StashServiceImpl;
import com.nxp.bj.tinker.utils.TinkerUtils;

public class TinkerTargetSourceReviewersServlet extends HttpServlet {
    
    private final PluginSettings pluginSettings;
    private final StashServiceImpl tinkerStashService;
    
    public TinkerTargetSourceReviewersServlet(
            StashServiceImpl tinkerStashService,
            PluginSettingsFactory pluginSettingsFactory) {
        this.tinkerStashService = tinkerStashService;
        this.pluginSettings = pluginSettingsFactory.createGlobalSettings();
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String targetSource = req.getParameter(Constants.KEY_OF_TARGET_SOURCE);
        String projKey = req.getParameter(Constants.KEY_OF_PROJECT_KEY);
        String reposName = req.getParameter(Constants.KEY_OF_REPOSITORY_NAME);
        
        req.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        JSONArray reposNameJsonArray = new JSONArray();
        
        if (!TinkerUtils.isProjReposMapped(projKey, reposName, pluginSettings)) {
            out.print(reposNameJsonArray.toString());
        }

        if (targetSource.startsWith(Constants.REF_PREFIX_TO_BRANCH)) {
            String featureName = targetSource.split(Constants.GIT_REF_SEPERATOR)[1];
            JSONArray reviewerEmails = TinkerUtils.getReviewerEmails(featureName);
            List<StashUser> stashUsers = tinkerStashService.getUserInfoByEmails(reviewerEmails);
           
            for (StashUser stashUser : stashUsers) {
                reposNameJsonArray.put(stashUser.getName());
            }
            
            out.print(reposNameJsonArray.toString());
        }

    }
}
