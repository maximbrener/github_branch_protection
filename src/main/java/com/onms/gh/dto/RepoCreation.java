package com.onms.gh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RepoCreation {
    private String name;
    @JsonProperty("private")
    private boolean privateRepo;
}
