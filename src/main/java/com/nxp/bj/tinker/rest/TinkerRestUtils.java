package com.nxp.bj.tinker.rest;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

public class TinkerRestUtils {
    private static String restURL = "http://sw-stash.freescale.net/rest/api/1.0/projects?name=DN QORIQ SDK";
    private static String baseGetProjectRestURL = "http://sw-stash.freescale.net/rest/api/1.0/projects?name=DN QORIQ SDK";
    
    private static final String restApiURL = "/rest/api/2/issue/";
    
    /**
     * Get the json of restful request
     * @param restRUL
     * @param paramMap
     * @return
     */
    private static String retrieveRestJson(String restRUL, Map<String, Object> paramMap) {
        RestTemplate restTemplate = new RestTemplate();

        return restTemplate.getForObject(restRUL, String.class, paramMap);
    }

    public static String retrieveJiraIssueStatus(String issueIdOrKey) {
        return issueIdOrKey;
    }

    public static void main(String[] args) {
        
    }
    
}
