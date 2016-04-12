package com.nxp.bj.tinker.service;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.project.Project;
import com.atlassian.stash.project.ProjectService;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.repository.RepositoryService;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.Page;
import com.atlassian.stash.util.PageRequest;
import com.atlassian.stash.util.PageRequestImpl;
import com.nxp.bj.tinker.service.interfaces.StashService;

public class StashServiceImpl implements StashService {
    private static final Logger logger = LoggerFactory.getLogger(StashServiceImpl.class);
    
    private final UserService userService;
    private final ProjectService projectService;
    private final RepositoryService repositoryService;
    
    public StashServiceImpl(
            UserService userService,
            ProjectService projectService,
            RepositoryService repositoryService) {
        this.userService = userService;
        this.projectService = projectService;
        this.repositoryService = repositoryService;
    }

    @Override
    public Map<String, String> retrieveAllCurrUserCanSeeProjectInfo() {
        List<String> projKeysList = projectService.findAllKeys();
        
        Map<String, String> projectInfo = new LinkedHashMap<String, String>();
        
        Project projObj = null;
        for (String projKey : projKeysList) {
            projObj = projectService.getByKey(projKey);
            projectInfo.put(projObj.getKey(), projObj.getName());
        }
        
        return projectInfo;
    }

    @Override
    public List<Repository> retrieveRepositoriesByProjectKey(String projectKey) {
        checkNotNull(projectKey, "project key is null");
        
        PageRequest pageRequest = new PageRequestImpl(0, PageRequest.MAX_PAGE_LIMIT);
        Page<? extends Repository> page = repositoryService.findByProjectKey(projectKey, pageRequest);
        
        List<Repository> reposList = new ArrayList<Repository>();

        Iterator<? extends Repository> iter = page.getValues().iterator();
        while (iter.hasNext()) {
            reposList.add(iter.next());
        }
        
        return reposList;
    }

    @Override
    public List<StashUser> getUserInfoByEmails(List<String> userEmailList) {
        List<StashUser> stashUserList = new ArrayList<StashUser>();
        
        StashUser stashUser = null;
        for (String userEmail : userEmailList) {
            stashUser = userService.findUserByNameOrEmail(userEmail);
            stashUserList.add(stashUser);
        }

        return stashUserList;
    }
    
    @Override
    public List<StashUser> getUserInfoByEmails(JSONArray userEmailList) {
        List<StashUser> stashUserList = new ArrayList<StashUser>();
        
        String userEmail = "";
        StashUser stashUser = null;
        for (int i = 0 ; i < userEmailList.length() ; i++) {
            userEmail = userEmailList.getString(i);
            stashUser = userService.findUserByNameOrEmail(userEmail);
            stashUserList.add(stashUser);
        }

        return stashUserList;
    }
}
