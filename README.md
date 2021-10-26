# Branch protection
To protect all main branches in given organization do the following steps:
1. Modify AUTH member in the Const class  with username and token
2. Modify ORGANIZATION member in the Const class with name of the organization you want to apply protection on
3. Run main() method in the GithubBranchProtection class

To see what branches were modified with protection see main_branches.csv file.

## Rollback applied branch protection
When we apply branch protection we also save current protection state of all the branches under the snapshot folder. To rollback to that previous state, do the following:
1. Run main() method in the GithubBranchProtectionRollback class