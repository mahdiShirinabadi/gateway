package com.eureka.acl.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectRegistrationRequest {

    private String name;
    private String description;
    private String baseUrl;
    private String version;
}
