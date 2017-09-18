package me.suwash.swagger.spec.manager.ws;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.util.JsonUtils;

import org.apache.commons.lang.StringUtils;
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

    protected static MockHttpServletRequestBuilder addRequestBody(
        final MockHttpServletRequestBuilder builder,
        final RequestMediaType mediaType,
        final Object payload,
        final CommitInfo commitInfo) {

        String requestBody = StringUtils.EMPTY;
        if (RequestMediaType.json.equals(mediaType)) {
            requestBody = JsonUtils.writeString(payload);

        } else if (RequestMediaType.yaml.equals(mediaType)) {
            requestBody = SwaggerSpecUtils.writeString(payload);
        }

        return addRequestHeader(
            builder
                .contentType(mediaType.value())
                .content(requestBody),
            commitInfo);
    }

    protected static MockHttpServletRequestBuilder addMediaType(
        final MockHttpServletRequestBuilder builder,
        final RequestMediaType mediaType,
        final CommitInfo commitInfo) {

        return addRequestHeader(
            builder.contentType(mediaType.value()),
            commitInfo);
    }

    protected static MockHttpServletRequestBuilder addRequestHeader(
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
