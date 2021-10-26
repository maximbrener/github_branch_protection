package com.onms.gh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onms.gh.dto.Branch;
import com.onms.gh.dto.Repo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class GithubClient {

   public static String getBranchProtection(String org, String repo, String branch){
        String url = "https://api.github.com/repos/" + org + "/" + repo + "/branches/" + branch + "/protection";
        return getGithubData(url);
    }

    public static Branch[] getAllBranchesInOrg(Repo repo, String org) {
        List<Branch> allBranches = new ArrayList<>();
        String repoName = repo.getName();
        boolean notEmpty = true;
        int i = 1;
        while (notEmpty) {
            System.out.println("Getting branches for repo: \"" + repoName + "\" with URL: https://api.github.com/repos/" + org + "/" + repoName + "/branches?per_page=100&page=" + i);
            String branchesJson = getGithubData("https://api.github.com/repos/" + org + "/" + repoName + "/branches?per_page=100&page=" + i);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            Branch[] branches = null;
            try {
                branches = objectMapper.readValue(branchesJson, Branch[].class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            if (branches.length == 100){
                Collections.addAll(allBranches, branches);
                i++;
            } else if(branches.length == 0){
                notEmpty = false;
            } else if(branches.length < 100){
                Collections.addAll(allBranches, branches);
                notEmpty = false;
            }
        }
        Branch[] branchesArray = new Branch[allBranches.size()];
        return allBranches.toArray(branchesArray);
    }

    public static Repo[] getAllReposInOrg(String org) {
        Repo[] repos = new Repo[0];
        String reposJson = getGithubData("https://api.github.com/orgs/" + org + "/repos?per_page=100");
        if (reposJson != null) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            try {
                repos = objectMapper.readValue(reposJson, Repo[].class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return repos;
    }

    public static String getGithubData(String urlStr) {
        String result = null;
        HttpURLConnection con = null;
        int status = 0;
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            byte[] encodedAuth = Base64.getEncoder().encode(Const.AUTH.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            con.setRequestProperty("Authorization", authHeaderValue);
            con.setRequestMethod("GET");
            status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            result = content.toString();
        } catch (Exception e) {
            if (status != 200) {
                System.out.println("Error:" + e.getMessage());
                result = null;
            }
        }
        return result;
    }

    public static String deleteGithubData(String urlStr) {
        String result = null;
        HttpURLConnection con = null;
        int status = 0;
        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            byte[] encodedAuth = Base64.getEncoder().encode(Const.AUTH.getBytes(StandardCharsets.UTF_8));
            String authHeaderValue = "Basic " + new String(encodedAuth);
            con.setRequestProperty("Authorization", authHeaderValue);
            con.setRequestMethod("DELETE");
            status = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();
            result = content.toString();
        } catch (Exception e) {
            if (status != 200) {
                System.out.println("Error:" + e.getMessage());
                result = null;
            }
        }
        return result;
    }

    public static void putGithubData(String url, String body) {
        HttpResponse<String> response = null;
        byte[] encodedAuth = Base64.getEncoder().encode(Const.AUTH.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", authHeaderValue)
                .header("Accept", "application/vnd.github.loki-preview+json")
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(response.statusCode());
        System.out.println(response.body());
    }
}
