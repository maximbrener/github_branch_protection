package com.onms.gh;

public class GithubRepoCreation {

    public static void main(String[] args) {
        String repoName = "task-mng-take-home";
        GithubClient.createRepository(Const.ORGANIZATION, repoName);
    }
}
