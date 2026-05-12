package com.uq.happypet.testrail;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TestRailProperties.class)
public class TestRailConfiguration {
}
