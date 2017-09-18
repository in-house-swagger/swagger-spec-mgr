package me.suwash.swagger.spec.manager.infra.config;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spec.mgr")
public class ApplicationProperties {
    private static final String DIRNAME_REPOSITORY = "repo";

    private Map<String, String> defaultCommitInfo;
    private Map<String, String> dirInfo;
    private List<String> splitIgnoreRegexList;
    private Map<String, String> scriptEnv;

    public String getUserDir(final CommitInfo commitInfo) {
        if (commitInfo == null || StringUtils.isEmpty(commitInfo.getUser()))
            return getDirData() + "/" + getDefaultCommitUser();
        return getDirData() + "/" + commitInfo.getUser();
    }

    public String getUserRepoDir(final CommitInfo commitInfo) {
        return getUserDir(commitInfo) + "/" + DIRNAME_REPOSITORY;
    }

    public String getDirSpecs(final CommitInfo commitInfo) {
        return getUserRepoDir(commitInfo) + "/" + dirInfo.get("specs");
    }

    public String getDefaultCommitUser() {
        return defaultCommitInfo.get("user");
    }
    public String getDefaultCommitMessage() {
        return defaultCommitInfo.get("message");
    }

    public String getDirBin() {
        return dirInfo.get("bin");
    }
    public String getDirData() {
        return dirInfo.get("data");
    }
}
