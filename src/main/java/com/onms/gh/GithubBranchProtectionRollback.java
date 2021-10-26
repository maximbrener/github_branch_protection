package com.onms.gh;



import java.io.IOException;



public class GithubBranchProtectionRollback {

    private static final String CURRENT_STATE_SNAPSHOT_DIR = "snapshot/";
    private static final String ORGANIZATION = "OpenNMS";

    private static final String ROLLBACK_BRANCH_PROTECTION_BODY = "{\n" +
            "  \"required_status_checks\": null,\n" +
            "  \"enforce_admins\": null,\n" +
            "  \"required_pull_request_reviews\": null,\n" +
            "  \"restrictions\": null\n" +
            "}";

    public static void main(String[] args) {
        GithubBranchProtectionRollback gbp = new GithubBranchProtectionRollback();

        //remove all branch protection
        gbp.rollbackBranchProtection(ORGANIZATION, CURRENT_STATE_SNAPSHOT_DIR);
    }

    private void rollbackBranchProtection(String org, String snapshotDir){
        String branchProtection = null;
        String repo = null;
        String branch = null;
        for (String file : Utils.listFiles(snapshotDir)) {
            String[] s = file.split("___");
            if (s.length < 3){
                continue;
            }
            repo = s[1];
            branch = s[2];
            String putEndpoint = "https://api.github.com/repos/" + org + "/" + repo + "/branches/" + branch + "/protection";
            try {
                branchProtection = Utils.readFile(snapshotDir, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (branchProtection.isBlank()){
                System.out.println("Deleting branch protection for repo: \"" + repo + "\" and branch: \"" + branch + "\" with URL: " + putEndpoint);
                GithubClient.deleteGithubData(putEndpoint);
            } else {

                System.out.println("Updating branch protection for repo: \"" + repo + "\" and branch: \"" + branch + "\" with URL: " + putEndpoint);
                GithubClient.putGithubData(putEndpoint, branchProtection);
            }
        }
    }
}
