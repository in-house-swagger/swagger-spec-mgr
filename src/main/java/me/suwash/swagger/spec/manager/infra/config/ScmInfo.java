package me.suwash.swagger.spec.manager.infra.config;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor()
@Getter
@EqualsAndHashCode
public class ScmInfo {
    @JsonProperty("user")
    @NotEmpty
    protected String user = null;

    @JsonProperty("email")
    @NotEmpty
    protected String email = null;
}
