package com.onms.gh;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onms.gh.dto.Team;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GithubTeamsManagement {

    private static final String COMMA_DELIMITER = ",";

    public static void main(String[] args) {
        GithubTeamsManagement githubTeamsManagement = new GithubTeamsManagement();
        Map teamMembers = githubTeamsManagement.getTeamMembers();
        System.out.println(teamMembers);
        githubTeamsManagement.setupTeamsAndMembers(Const.ORGANIZATION, teamMembers);
    }

    public void setupTeamsAndMembers(String org, Map<String, List<String>> teamMembers) {
        addTeams(Const.ORGANIZATION, teamMembers);
        addTeamMembers(Const.ORGANIZATION, teamMembers);
    }

    public Map getTeamMembers() {
        Map<String, List<String>> teamToMembersMap = new HashMap<>();
        List<List<String>> records = readOrganizationMappingFile();
        boolean header = true;
        for (List<String> record : records) { // iterate over rows
            if (header){ // skipping header row
                header = false;
                List<String> teamsList = record.subList(1, record.size());
                for (String teamName : teamsList) {
                    teamToMembersMap.put(teamName, new ArrayList<>());
                }
                continue;
            }

            boolean gitUsernameColumn = true;
            String gitUsername = null;
            for (String cell : record) { // iterate over cells in one row
                if (gitUsernameColumn) { // save git username and move to next cell
                    gitUsername = cell;
                    gitUsernameColumn = false;
                    continue;
                }

                if (!cell.isBlank()) {
                    if (teamToMembersMap.get(cell) == null) {
                        teamToMembersMap.put(cell, new ArrayList<>());
                    }
                    List<String> members = teamToMembersMap.get(cell);
                    members.add(gitUsername);
                }
            }
        }
        return teamToMembersMap;
    }

    public List<List<String>> readOrganizationMappingFile() {
        List<List<String>> records = new ArrayList<>();
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(Const.CSV_FILE);
        InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        try (BufferedReader br = new BufferedReader(streamReader)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                records.add(Arrays.asList(values));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public void addTeams(String org, Map<String, List<String>> map) {
        for (String team : map.keySet()) {
            addTeam(org, team.trim());
        }
    }

    public void addTeam(String org, String teamName) {
        String postEndpoint = "https://api.github.com/orgs/" + org + "/teams";
        Team team = new Team();
        team.setName(teamName);
        team.setPrivacy("secret");
        team.setPermission("pull");
        var objectMapper = new ObjectMapper();
        String body = null;
        try {
            body = objectMapper.writeValueAsString(team);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println("Adding team: " + teamName + " using URL: " + postEndpoint);
        GithubClient.postGithubData(postEndpoint, body);
    }

    public void addTeamMembers(String org, Map<String, List<String>> teamMembers) {
        for (String teamName : teamMembers.keySet()) {
           List<String> usernames = teamMembers.get(teamName);
            for (String username : usernames) {
                String putEndpoint = "https://api.github.com/orgs/" + org + "/teams/" + teamName.toLowerCase().replace(" ", "-") + "/memberships/" + username.trim();
                System.out.println("Updating team: \"" + teamName.toLowerCase().replace(" ", "-") + "\" with member: \"" + username + "\" using URL: " + putEndpoint);
                GithubClient.putGithubData(putEndpoint, "");
            }
        }
    }
}
