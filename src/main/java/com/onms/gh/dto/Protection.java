package com.onms.gh.dto;

import lombok.Data;

@Data
public class Protection{
    public boolean enabled;
    public RequiredStatusChecks required_status_checks;
}
