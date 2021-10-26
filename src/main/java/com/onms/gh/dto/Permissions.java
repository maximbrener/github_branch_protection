package com.onms.gh.dto;

import lombok.Data;

@Data
public class Permissions {
    public boolean admin;
    public boolean maintain;
    public boolean push;
    public boolean triage;
    public boolean pull;
}
