package com.nxp.bj.tinker;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.atlassian.stash.repository.Repository;
import com.nxp.bj.tinker.constants.Constants;
import com.nxp.bj.tinker.service.StashServiceImpl;

import org.json.JSONArray;


public class TinkerRetrieveReposServlet extends HttpServlet {
    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -1996738358209174290L;

    private final Logger logger = LoggerFactory.getLogger(TinkerRetrieveReposServlet.class);
    
    private final StashServiceImpl tinkerStashService;
    
    public TinkerRetrieveReposServlet(
            StashServiceImpl tinkerStashService) {
        this.tinkerStashService = tinkerStashService;
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String projKey = req.getParameter(Constants.KEY_OF_PROJECT_KEY);

        req.setCharacterEncoding("utf8");
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        JSONArray reposNameJsonArray = new JSONArray();
        
        List<Repository> reposObjList = tinkerStashService.retrieveRepositoriesByProjectKey(projKey);
        
        for (Repository repos : reposObjList) {
            reposNameJsonArray.put(repos.getName());
        }
        
        out.print(reposNameJsonArray.toString());
    }
}
