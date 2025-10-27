package com.onms.gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onms.gh.dto.Branch;
import com.onms.gh.dto.Repo;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to review branch protection status for a specific GitHub repository
 */
public class GithubRepoReview {

    private static final String REVIEW_REPORT_FILE = "review_report.txt";

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java GithubRepoReview <owner/repo>");
            System.out.println("Example: java GithubRepoReview minjibir/task-management-service");
            return;
        }

        String[] parts = args[0].split("/");
        if (parts.length != 2) {
            System.out.println("Invalid repository format. Use: owner/repo");
            System.out.println("Example: minjibir/task-management-service");
            return;
        }

        String owner = parts[0];
        String repoName = parts[1];

        GithubRepoReview reviewer = new GithubRepoReview();
        reviewer.reviewRepository(owner, repoName);
    }

    /**
     * Review branch protection for a specific repository
     */
    public void reviewRepository(String owner, String repoName) {
        System.out.println("=".repeat(80));
        System.out.println("Branch Protection Review Report");
        System.out.println("Repository: " + owner + "/" + repoName);
        System.out.println("=".repeat(80));
        System.out.println();

        // Get repository info
        Repo repo = getRepositoryInfo(owner, repoName);
        if (repo == null) {
            System.out.println("ERROR: Could not retrieve repository information.");
            System.out.println("Please check:");
            System.out.println("  1. Repository exists and is accessible");
            System.out.println("  2. Credentials in Const.AUTH are valid");
            System.out.println("  3. You have permissions to access this repository");
            return;
        }

        System.out.println("Repository Information:");
        System.out.println("  Name: " + repo.getName());
        System.out.println("  Full Name: " + repo.getFull_name());
        System.out.println("  Private: " + repo.isPrivateRepo());
        System.out.println("  Archived: " + repo.isArchived());
        System.out.println("  Default Branch: " + repo.getDefault_branch());
        System.out.println();

        if (repo.isArchived()) {
            System.out.println("WARNING: This repository is archived.");
            System.out.println();
        }

        // Get all branches
        Branch[] branches = GithubClient.getAllBranches(repo, owner);
        System.out.println("Total Branches Found: " + branches.length);
        System.out.println();

        // Identify main branches
        List<Branch> mainBranches = findMainBranches(branches);
        System.out.println("Main Branches (develop, main, master, trunk, release*, foundation*): " + mainBranches.size());
        System.out.println();

        // Review each main branch
        System.out.println("-".repeat(80));
        System.out.println("Branch Protection Details:");
        System.out.println("-".repeat(80));
        System.out.println();

        int protectedCount = 0;
        int unprotectedCount = 0;

        for (Branch branch : mainBranches) {
            System.out.println("Branch: " + branch.getName());
            System.out.println("  Protected: " + branch.isProtectedBranch());

            if (branch.isProtectedBranch()) {
                protectedCount++;
                String protectionData = GithubClient.getBranchProtection(owner, repoName, branch.getName());
                if (protectionData != null && !protectionData.isEmpty()) {
                    printProtectionDetails(protectionData);
                } else {
                    System.out.println("  WARNING: Branch marked as protected but no protection rules found");
                }
            } else {
                unprotectedCount++;
                System.out.println("  ⚠ RECOMMENDATION: Consider adding branch protection");
            }
            System.out.println();
        }

        // Summary
        System.out.println("=".repeat(80));
        System.out.println("Summary:");
        System.out.println("=".repeat(80));
        System.out.println("Total Main Branches: " + mainBranches.size());
        System.out.println("Protected: " + protectedCount);
        System.out.println("Unprotected: " + unprotectedCount);
        System.out.println();

        if (unprotectedCount > 0) {
            System.out.println("⚠ WARNING: " + unprotectedCount + " main branch(es) lack protection!");
            System.out.println();
            System.out.println("Recommended Actions:");
            System.out.println("  1. Enable branch protection for all main branches");
            System.out.println("  2. Require pull request reviews before merging");
            System.out.println("  3. Require status checks to pass before merging");
            System.out.println("  4. Consider requiring administrator review for critical branches");
        } else {
            System.out.println("✓ All main branches are protected!");
        }
        System.out.println();
        System.out.println("=".repeat(80));
    }

    /**
     * Get repository information
     */
    private Repo getRepositoryInfo(String owner, String repoName) {
        String repoJson = GithubClient.getGithubData("https://api.github.com/repos/" + owner + "/" + repoName);
        if (repoJson == null || repoJson.isEmpty()) {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(repoJson, Repo.class);
        } catch (Exception e) {
            System.out.println("Error parsing repository data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find main branches from all branches
     */
    private List<Branch> findMainBranches(Branch[] branches) {
        List<Branch> mainBranches = new ArrayList<>();
        for (Branch branch : branches) {
            if (isMainBranch(branch.getName())) {
                mainBranches.add(branch);
            }
        }
        return mainBranches;
    }

    /**
     * Check if a branch name is considered a main branch
     */
    private boolean isMainBranch(String branchName) {
        return branchName.equals("develop") ||
                branchName.equals("main") ||
                branchName.equals("master") ||
                branchName.equals("trunk") ||
                branchName.startsWith("foundation") ||
                branchName.startsWith("release");
    }

    /**
     * Print detailed protection information
     */
    private void printProtectionDetails(String protectionJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(protectionJson);

            // Required status checks
            if (root.has("required_status_checks") && !root.get("required_status_checks").isNull()) {
                JsonNode statusChecks = root.get("required_status_checks");
                System.out.println("  Required Status Checks:");
                System.out.println("    Strict: " + statusChecks.path("strict").asBoolean());
                if (statusChecks.has("contexts")) {
                    JsonNode contexts = statusChecks.get("contexts");
                    if (contexts.isArray() && contexts.size() > 0) {
                        System.out.println("    Contexts:");
                        for (JsonNode context : contexts) {
                            System.out.println("      - " + context.asText());
                        }
                    } else {
                        System.out.println("    Contexts: None");
                    }
                }
            }

            // Required pull request reviews
            if (root.has("required_pull_request_reviews") && !root.get("required_pull_request_reviews").isNull()) {
                JsonNode prReviews = root.get("required_pull_request_reviews");
                System.out.println("  Required Pull Request Reviews:");
                System.out.println("    Dismiss stale reviews: " + prReviews.path("dismiss_stale_reviews").asBoolean());
                System.out.println("    Require code owner reviews: " + prReviews.path("require_code_owner_reviews").asBoolean());
                System.out.println("    Required approving review count: " + prReviews.path("required_approving_review_count").asInt());
            }

            // Enforce admins
            if (root.has("enforce_admins") && !root.get("enforce_admins").isNull()) {
                JsonNode enforceAdmins = root.get("enforce_admins");
                System.out.println("  Enforce Admins: " + enforceAdmins.path("enabled").asBoolean());
            }

            // Restrictions
            if (root.has("restrictions") && !root.get("restrictions").isNull()) {
                System.out.println("  Push Restrictions: Enabled");
            }

        } catch (Exception e) {
            System.out.println("  Error parsing protection details: " + e.getMessage());
        }
    }
}
