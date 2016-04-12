package com.nxp.bj.tinker;

import org.json.JSONObject;

public class PluginMetadata {
    
    public static String getPluginKey() {
        return "nxp.bj.nxp-workflow-tinker";
    }
    
    public static String getCompleteModuleKey(String moduleKey) {
        return getPluginKey() + ":" + moduleKey;
    }
    
    
}
