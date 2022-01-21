package com.onms.gh.dto;

import lombok.Data;

@Data
public class Team {
    public String name;
    public int id;
    public String node_id;
    public String slug;
    public String description;
    public String privacy;
    public String url;
    public String html_url;
    public String members_url;
    public String repositories_url;
    public String permission;
    public Object parent;
}
