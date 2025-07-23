package com.sophium.treeier.config.security.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "treeier.security.csrf")
public class CsrfProperties {

    private String domain;

    private String token;
}

