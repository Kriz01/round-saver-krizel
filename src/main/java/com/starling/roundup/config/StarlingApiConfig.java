package com.starling.roundup.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("starling.api")
@Getter
@Setter
public class StarlingApiConfig {
    private String baseUrl;
}

