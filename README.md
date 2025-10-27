# Branch protection

## Review branch protection for a specific repository
To review the branch protection status of a single repository:
1. Modify AUTH member in the Const class with username and token
2. Run: `mvn compile exec:java -Dexec.mainClass="com.onms.gh.GithubRepoReview" -Dexec.args="owner/repo"`
   - Example: `mvn compile exec:java -Dexec.mainClass="com.onms.gh.GithubRepoReview" -Dexec.args="minjibir/task-management-service"`

The review will generate a detailed report showing:
- Repository information
- All main branches (main, master, develop, trunk, release*, foundation*)
- Branch protection status for each main branch
- Detailed protection rules (if protected)
- Recommendations for unprotected branches

## Apply branch protection to all repositories in an organization
To protect all main branches in given organization do the following steps:
1. Modify AUTH member in the Const class  with username and token
2. Modify ORGANIZATION member in the Const class with name of the organization you want to apply protection on
3. Run main() method in the GithubBranchProtection class

To see what branches were modified with protection see main_branches.csv file.

## Rollback applied branch protection
When we apply branch protection we also save current protection state of all the branches under the snapshot folder. To rollback to that previous state, do the following:
1. Run main() method in the GithubBranchProtectionRollback class