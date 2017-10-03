package me.suwash.swagger.spec.manager.infra.config;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "spec.mgr")
public class ApplicationProperties {
  private static final String DIRNAME_REPOSITORY = "repo";
  private static final String DEFAULT_EMAIL_DOMAIN = "@domain.local";

  private Map<String, String> defaultCommitInfo;
  private Map<String, String> dirInfo;
  private List<String> splitIgnoreRegexList;
  private Map<String, String> cors;
  private Map<String, String> scriptEnv;

  /**
   * コミットユーザディレクトリを返します。
   *
   * @param commitInfo コミット情報
   * @return コミットユーザディレクトリ
   */
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

  public String getDefaultCommitEmail() {
    return getDefaultCommitUser() + DEFAULT_EMAIL_DOMAIN;
  }

  public String getAllowOrigin() {
    if (cors == null) return null;
    return cors.get("allowOrigin");
  }

  public String getAllowMethods() {
    if (cors == null) return null;
    return cors.get("allowMethods");
  }

  public String getAllowHeaders() {
    if (cors == null) return null;
    return cors.get("allowHeaders");
  }
}
