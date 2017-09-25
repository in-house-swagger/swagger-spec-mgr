package me.suwash.swagger.spec.manager.ws.api;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;

import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

public class ControllerTestUtils {

    protected enum RequestMediaType {
        json("application/json"),
        yaml("application/x-yaml");

        private String value;

        RequestMediaType(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }
    }

    protected static MockHttpServletRequestBuilder withCommitInfo(
        final MockHttpServletRequestBuilder builder,
        final CommitInfo commitInfo) {
        if (commitInfo == null) {
            return builder;

        } else {
            return builder
                .header("x-commit-user", commitInfo.getUser())
                .header("x-commit-email", commitInfo.getEmail())
                .header("x-commit-message", commitInfo.getMessage());
        }
    }

}
