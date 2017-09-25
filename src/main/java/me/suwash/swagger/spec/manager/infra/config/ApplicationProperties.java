package me.suwash.swagger.spec.manager.infra.config;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spec.mgr")
public class ApplicationProperties {
    private String defaultScmUser;
    private String dirData;
    private String relDirSpecs;
    private List<String> splitIgnoreRegexList;
}
