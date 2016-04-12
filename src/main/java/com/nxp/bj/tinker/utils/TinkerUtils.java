package com.nxp.bj.tinker.utils;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.UrlMode;
import com.atlassian.sal.api.pluginsettings.PluginSettings;
import com.atlassian.stash.pull.PullRequest;
import com.google.common.base.Joiner;
import com.nxp.bj.tinker.constants.Constants;

public class TinkerUtils {
    public static String getBaseUrl(ApplicationProperties applicationProperties, UrlMode urlMode) {
        return applicationProperties.getBaseUrl(urlMode);
    }
    
    public static String getPluginKey() {
        return "nxp.bj.nxp-workflow-tinker";
    }
    
    public static String getCompleteModuleKey(String moduleKey) {
        return getPluginKey() + ":" + moduleKey;
    }
    
    public static List<String> convertReposArrayToList(String reposArrayString) {
        String[] reposNameArr = reposArrayString.replace("[", "").replace("]", "").split(",");
        
        List<String> reposNameList = new ArrayList<String>();
        for (int index = 0 ; index < reposNameArr.length ; index++) {
            reposNameList.add(reposNameArr[index].trim());
        }
        
        return reposNameList;
    }
    
    public static Map<String, String> parseFromRefBranchInfo(PullRequest pullRequest) {
        checkNotNull(pullRequest, "PullRequest can not be null");
        
        Map<String, String> refBranchInfoMap = new HashMap<String, String>();
        
        String[] refIds = pullRequest.getFromRef().getDisplayId().split(Constants.GIT_REF_SEPERATOR);
        refBranchInfoMap.put(Constants.KEY_OF_REF_PREFIX, refIds[0]);
        
        if (refIds.length >= 2) {
            refBranchInfoMap.put(Constants.KEY_OF_REF_FEATURE_NAME, refIds[1]);
        }
        
        if (refIds.length >= 3) {
            refBranchInfoMap.put(Constants.KEY_OF_REF_JIRA_TICKET, refIds[2]);
        }
        
        return refBranchInfoMap;
    }

    public static Map<String, String> parseToRefBranchInfo(PullRequest pullRequest) {
        checkNotNull(pullRequest, "PullRequest can not be null");

        Map<String, String> refBranchInfoMap = new HashMap<String, String>();

        String[] refIds = pullRequest.getToRef().getDisplayId().split(Constants.GIT_REF_SEPERATOR);

        refBranchInfoMap.put(Constants.KEY_OF_REF_PREFIX, refIds[0]);

        if (refIds.length >= 2) {
            refBranchInfoMap.put(Constants.KEY_OF_REF_FEATURE_NAME, refIds[1]);
        }

        return refBranchInfoMap;
    }

    public static String getIPOwner(String featureName) {
        return new JSONObject(getIPOwnerJson()).getJSONArray(featureName).getString(2);
    }

    public static JSONArray getReviewerEmails(String featureName) {
        return new JSONObject(getIpOwnerReviewersAsJson()).getJSONArray(featureName);
    }
    
    public static boolean isProjReposMapped(PullRequest pullRequest, PluginSettings pluginSettings) {
        String toRefProjKey = pullRequest.getToRef().getRepository().getProject().getKey();
        String toRefReposName = pullRequest.getToRef().getRepository().getName();
        
        return isProjReposMapped(toRefProjKey, toRefReposName, pluginSettings);
    }

    public static boolean isProjReposMapped(String toRefProjKey, String toRefReposName, PluginSettings pluginSettings) {
        Map<String, Object> projReposInfo = (Map<String, Object>) pluginSettings.get(Constants.KEY_OF_TINKER_PROJ_REPOS_STORAGE);

        if(projReposInfo.containsKey(toRefProjKey)) {
            String reposNameListStr = (String)projReposInfo.get(toRefProjKey);
            List<String> reposNameList = TinkerUtils.convertReposArrayToList(reposNameListStr);
            if (reposNameList.contains(toRefReposName)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void saveTickets(PluginSettings pluginSettings, String projKey, String reposName, String feature, String issueKey) {
        
        String ticketStorageKey = getTicketStorageKey(projKey, reposName, feature);
                
        String orginIssueKeys = (String)pluginSettings.get(ticketStorageKey);
        if (orginIssueKeys == null || "".equals(orginIssueKeys)) {
            pluginSettings.put(ticketStorageKey , issueKey);            
        } else {
            List<String> issueKeyArrayList = Arrays.asList(orginIssueKeys.split(","));
            List<String> issueKeyList = new ArrayList<String>();

            issueKeyList.addAll(issueKeyArrayList);
            if (!issueKeyList.contains(issueKey)) {
                issueKeyList.add(issueKey);
            }

            String issueKeys = Joiner.on(",").join(issueKeyList);
            pluginSettings.put(ticketStorageKey, issueKeys);
        }
    }

    public static List<String> getSavedTickets(PluginSettings pluginSettings, String projKey, String reposName, String feature) {
        
        String ticketStorageKey = getTicketStorageKey(projKey, reposName, feature);

        String orginIssueKeys = (String)pluginSettings.get(ticketStorageKey);
        List<String> issueKeyList = new ArrayList<String>();
        if (orginIssueKeys != null) {
            List<String> issueKeyArrayList = Arrays.asList(orginIssueKeys.split(","));
            
            issueKeyList.addAll(issueKeyArrayList);
        }
        
        return issueKeyList;
    }

    public static void removeStorageTickets(PluginSettings pluginSettings, String projKey, String reposName, String feature) {
        String ticketStorageKey = getTicketStorageKey(projKey, reposName, feature);

        pluginSettings.remove(ticketStorageKey);
    }
    
    private static String getTicketStorageKey(String projKey, String reposName, String feature) {
        StringBuilder sb = new StringBuilder();
        sb.append(Constants.KEY_OF_TINKER_PLUGIN_STORAGE);
        sb.append(".").append(projKey)
          .append(".").append(reposName)
          .append(".").append(feature);
        return sb.toString();
    }

    public static boolean isJiraBranch(String branchPrefix) {
        if (Constants.REF_PREFIX_FROM_BRANCH.equals(branchPrefix)) {
            return true;
        } else {
            return false;
        }
    }
    
    public static String getIpOwnerReviewersAsJson() {
        return "{\"asf\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
                + "\"audio\": [\"b32145@example.com\", \"b52813@freescale.com\"],"
            +"\"autoresp\": [\"b32145@example.com\"],"
            + "\"bman\": [],"
            + "\"caam\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"can\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"capwap\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"clock\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"config\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"core\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"crypto\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"dce\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"dcu\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"diu\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"dma\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"dpaa-ethernet\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"elbc\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            + "\"etsec\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"flextimer\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"fman\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"gpio\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"hugetlb\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"i2c\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"ieee1588\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"ifc\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"kvm\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"l2switch\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"lag\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"lpuart\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"lxc\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"mdio\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"mmu\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"mpic\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"nas\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"pamu\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"pci\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"perf\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"phy\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"pme\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"powermanagement\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"qe\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"qman\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"raid\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"rman\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"rt\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"sata\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"sdhc\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"smmu\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"srio\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"ssl\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"tdm\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"usb\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"vfio\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"watchdog\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"],"
            +"\"poc\": [\"b32145@example.com\", \"b52813@freescale.com\", \"b12345@example.com\"]}";
    }

    public static String getIPOwnerJson() {
        return "{\"asf\": [\"QASF\", \"ALL/GENERAL\", \"B02416\"],"
                + "\"audio\": [\"QSDK\", \"AUDIO\", \"B18965\"],"
            +"\"autoresp\": [\"QSDK\", \"AUTO-RESPONSE\", \"B15745\"],"
            + "\"bman\": [\"QLINUX\", \"DPAA-QBMAN\",\"R01356\"],"
            + "\"caam\": [\"QLINUX\", \"SEC\",\"R19439\"],"
            +"\"can\": [\"QLINUX\", \"FLEXCAN\",\"B45370\"],"
            +"\"capwap\": [\"QLINUX\", \"CAPWAP\", \"B37022\"],"
            +"\"clock\": [\"QLINUX\", \"CLOCK\", \"B44306\"],"
            +"\"config\": [\"QLINUX\", \"CONFIG\", \"B44306\"],"
            +"\"core\": [\"QLINUX\", \"CORE-POWER\", \"B07421\"],"
            +"\"crypto\": [\"QLINUX\", \"SEC\", \"R19439\"],"
            +"\"dce\": [\"QLINUX\", \"DCE\", \"R01356\"],"
            +"\"dcu\": [\"QLINUX\", \"DCU\", \"B18965\"],"
            +"\"diu\": [\"QLINUX\", \"DIU\", \"B40534\"],"
            +"\"dma\": [\"QLINUX\", \"DMA\", \"B46683\"],"
            +"\"dpaa-ethernet\": [\"QLINUX\", \"DPAA-ETHERNET\", \"B32716\"],"
            +"\"elbc\": [\"QLINUX\", \"LBC-NOR-NAND\", \"B32579\"],"
            + "\"etsec\": [\"QLINUX\", \"TSEC-ETHERNET\", \"B08782\"],"
            +"\"flextimer\": [\"QLINUX\", \"FLEXTIMER\", \"B40534\"],"
            +"\"fman\": [\"QLINUX\", \"DPAA-FMAN\", \"R52568\"],"
            +"\"gpio\": [\"QLINUX\", \"GPIO\", \"B34182\"],"
            +"\"hugetlb\": [\"QLINUX\", \"CORE-POWER\", \"B07421\"],"
            +"\"i2c\": [\"QLINUX\", \"I2C\", \"B40530\"],"
            +"\"ieee1588\": [\"QLINUX\", \"IEEE1588\", \"B47093\"],"
            +"\"ifc\": [\"QLINUX\", \"IFC-NOR-NAND\", \"B32579\"],"
            +"\"kvm\": [\"QLINUX\", \"KVM\", \"B02008\"],"
            +"\"l2switch\": [\"QLINUX\", \"T1040-L2SWITCH\", \"BOGVLAD1\"],"
            +"\"lag\": [\"QLINUX\", \"DPAA-LAG\", \"B29408\"],"
            +"\"lpuart\": [\"QLINUX\", \"UART\", \"B46683\"],"
            +"\"lxc\": [\"QLINUX\", \"LXC\", \"B43198\"],"
            +"\"mdio\": [\"QLINUX\", \"MDIO-PHY\", \"B21989\"],"
            +"\"mmu\": [\"QLINUX\", \"CORE-POWER\", \"B07421\"],"
            +"\"mpic\": [\"QLINUX\", \"CORE-POWER\", \"B07421\"],"
            +"\"nas\": [\"QSDK\", \"NAS\", \"B29408\"],"
            +"\"pamu\": [\"QLINUX\", \"PAMU\", \"B16395\"],"
            +"\"pci\": [\"QLINUX\", \"PCI\", \"B21284\"],"
            +"\"perf\": [\"QLINUX\", \"TOOLS-PERF\", \"RAT063\"],"
            +"\"phy\": [\"QLINUX\", \"MDIO-PHY\", \"B21989\"],"
            +"\"pme\": [\"QLINUX\", \"PME\", \"R01356\"],"
            +"\"powermanagement\": [\"QLINUX\", \"POWER-MGMT\", \"R64188\"],"
            +"\"qe\": [\"QLINUX\", \"QUICC-ENGINE\", \"B45475\"],"
            +"\"qman\": [\"QLINUX\", \"QBMAN\", \"R01356\"],"
            +"\"raid\": [\"QLINUX\", \"RAID\", \"B29237\"],"
            +"\"rman\": [\"QLINUX\", \"RMAN\", \"B31939\"],"
            +"\"rt\": [\"QLINUX\", \"REAL-TIME\", \"B32167\"],"
            +"\"sata\": [\"QLINUX\", \"SATA\", \"B29983\"],"
            +"\"sdhc\": [\"QLINUX\", \"SDHC\", \"R63061\"],"
            +"\"smmu\": [\"QLINUX\", \"SMMU\", \"B16395\"],"
            +"\"srio\": [\"QLINUX\", \"SRIO\", \"B34182\"],"
            +"\"ssl\": [\"QLINUX\", \"SEC\", \"R19439\"],"
            +"\"tdm\": [\"QLINUX\", \"TDM\", \"B10812\"],"
            +"\"usb\": [\"QLINUX\", \"USB\", \"B31383\"],"
            +"\"vfio\": [\"QLINUX\", \"VFIO\", \"R65777\"],"
            +"\"watchdog\": [\"QLINUX\", \"WATCHDOG\", \"B40634\"],"
            +"\"poc\": [\"SDKSND\", \"Component 1\", \"B43082\"]}";
    }
}
