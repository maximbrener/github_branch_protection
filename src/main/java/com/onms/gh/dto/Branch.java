package com.onms.gh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Branch {
    public String name;
    public Commit commit;
    @JsonProperty("protected")
    public boolean protectedBranch;
    public Protection protection;
    public String protection_url;

    public String toString(){
        return name;
    }
}
