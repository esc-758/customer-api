package com.earlycharlemagne.customerapi.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Configuration
@ConfigurationProperties("customerapi.security")
public class SecurityConfigProperties {
    private String username;
    private String password;
}
