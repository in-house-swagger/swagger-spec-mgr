package me.suwash.swagger.spec.manager.ws.api;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;

public class ControllerTestUtils {

  protected enum RequestMediaType {
    json("application/json"), yaml("application/x-yaml");

    private String value;

    RequestMediaType(String value) {
      this.value = value;
    }

    String value() {
      return this.value;
    }
  }

  protected static MockHttpServletRequestBuilder withCommitInfo(
      final MockHttpServletRequestBuilder builder, final CommitInfo commitInfo) {
    if (commitInfo == null) {
      return builder;

    } else {
      return builder.header("X-Commit-User", commitInfo.getUser())
          .header("x-commit-email", commitInfo.getEmail())
          .header("X-Commit-Message", commitInfo.getMessage());
    }
  }

}
