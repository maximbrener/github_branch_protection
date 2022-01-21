package com.onms.gh.dto;


import lombok.Data;

import java.util.List;
@Data
public class RequiredStatusChecks{
    public String enforcement_level;
    public List<Object> contexts;
    public List<Object> checks;
}

