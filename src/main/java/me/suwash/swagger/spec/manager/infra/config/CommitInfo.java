package me.suwash.swagger.spec.manager.infra.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor()
@Getter
public class CommitInfo {

    public CommitInfo(final String user, final String email) {
        this.user = user;
        this.email = email;
    }

    @JsonProperty("user")
    @NotEmpty
    protected String user = null;

    @JsonProperty("email")
    @NotEmpty
    protected String email = null;

    @JsonProperty("message")
    protected String message = null;

}
