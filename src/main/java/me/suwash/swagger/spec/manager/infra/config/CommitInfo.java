package me.suwash.swagger.spec.manager.infra.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;

@AllArgsConstructor()
@Getter
public class CommitInfo {

  public CommitInfo(final String user, final String email) {
    this.user = user;
    this.email = email;
  }

  protected String user = null;

  protected String email = null;

  protected String message = null;

  public void canInit() {
    ValidationUtils.mustNotEmpty("user", user);
    ValidationUtils.mustNotEmpty("email", email);
  }

}
