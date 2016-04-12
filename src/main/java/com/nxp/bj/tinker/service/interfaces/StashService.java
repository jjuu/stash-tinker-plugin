package com.nxp.bj.tinker.service.interfaces;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.Repository;
import com.atlassian.stash.user.StashUser;

/**
 * Service object to serve for stash
 * @author B52813
 *
 */
public interface StashService {
    public List<StashUser> getUserInfoByEmails(List<String> userEmailList);
    public List<StashUser> getUserInfoByEmails(JSONArray userEmailList);
    public Map<String, String> retrieveAllCurrUserCanSeeProjectInfo();
    public List<Repository> retrieveRepositoriesByProjectKey(String projectKey);
    
}
