package com.onms.gh;


import com.onms.gh.dto.Branch;
import com.onms.gh.dto.Repo;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class GithubBranchProtection {

    private static final String CSV_FILE_NAME = "main_branches.csv";
    private static final String CURRENT_STATE_SNAPSHOT_DIR = "snapshot/";
    private static final String ORGANIZATION = "OpenNMS";

    private static final String BRANCH_PROTECTION_BODY = "{\n" +
            "  \"required_status_checks\": {\n" +
            "    \"strict\": true,\n" +
            "    \"contexts\": []\n" +
            "  },\n" +
            "  \"enforce_admins\": null,\n" +
            "  \"required_pull_request_reviews\": {\n" +
            "    \"dismissal_restrictions\": {},\n" +
            "    \"dismiss_stale_reviews\": true,\n" +
            "    \"require_code_owner_reviews\": false,\n" +
            "    \"required_approving_review_count\": 1\n" +
            "  },\n" +
            "  \"restrictions\": null\n" +
            "}";


    public static void main(String[] args) {
        GithubBranchProtection gbp = new GithubBranchProtection();
        Map<String, List<Branch>> reposWithMainBranches = gbp.findMainBranches(ORGANIZATION);

        // print to file
        Utils.printCSV(reposWithMainBranches, CSV_FILE_NAME);

        // print to console
        System.out.println("Total number of repositories: " + reposWithMainBranches.size());
        for (String repo : reposWithMainBranches.keySet()) {
            System.out.println("Repo: \"" + repo + "\", Main branches: " + reposWithMainBranches.get(repo));
        }

        // save current status of the protected branch
        gbp.saveCurrentState(reposWithMainBranches, ORGANIZATION, CURRENT_STATE_SNAPSHOT_DIR);

        //apply branch protection
        gbp.setBranchProtection(reposWithMainBranches, ORGANIZATION, BRANCH_PROTECTION_BODY);
    }

    private void setBranchProtection(Map<String, List<Branch>> reposWithMainBranches, String org, String protectionRules){
        for (String repo : reposWithMainBranches.keySet()) {
            for (Branch branch : reposWithMainBranches.get(repo)) {
                String putEndpoint = "https://api.github.com/repos/" + org + "/" + repo + "/branches/" + branch.getName() + "/protection";
                System.out.println("Updating branch protection for repo: \"" + repo + "\" and branch: \"" + branch.getName() + "\" with URL: " + putEndpoint);
                GithubClient.putGithubData(putEndpoint, protectionRules);
            }
        }
    }

    private void saveCurrentState(Map<String, List<Branch>> reposWithMainBranches, String org, String snapshotDir){
        for (String repo : reposWithMainBranches.keySet()) {
            for (Branch branch : reposWithMainBranches.get(repo)) {
                if (branch.isProtectedBranch()){
                    String protectionData = GithubClient.getBranchProtection(org, repo, branch.getName());
                    saveProtectionState(org, repo, branch.getName(), snapshotDir, protectionData);
                } else {
                    saveProtectionState(org, repo, branch.getName(), snapshotDir);
                }
            }
        }
    }

    private void saveProtectionState(String org, String repo, String branch, String snapshotDir){
        saveProtectionState(org, repo, branch, snapshotDir, "");
    }

    private void saveProtectionState(String org, String repo, String branch, String snapshotDir, String protectionData){
        String fileName = snapshotDir + org + "___" + repo + "___" + branch;
        File file = new File(snapshotDir);
        if (!file.exists()){
            file.mkdir();
        }
        if (!Files.exists(Path.of(fileName))) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(fileName));
                writer.write(protectionData);
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Map<String, List<Branch>> findMainBranches(String org){
        Map<String, List<Branch>> reposBranchesMap = new HashMap();
        List<Branch> mainBranches = new ArrayList<Branch>();
        for (Repo repo : GithubClient.getRepos(org)) {
            if (!repo.isArchived()) {
                for (Branch branch : GithubClient.getBranches(repo, org)) {
                    if (branch.getName().equals("develop") || branch.getName().equals("main") || branch.getName().equals("master") ||
                            branch.getName().startsWith("foundation") || branch.getName().startsWith("release") || branch.getName().equals("trunk")) {
                        if (!mainBranches.contains(branch)) {
                            mainBranches.add(branch);
                        }
                    }
                }
                reposBranchesMap.put(repo.getName(), mainBranches);
                mainBranches = new ArrayList<>();
            }
        }
        return reposBranchesMap;
    }
}
