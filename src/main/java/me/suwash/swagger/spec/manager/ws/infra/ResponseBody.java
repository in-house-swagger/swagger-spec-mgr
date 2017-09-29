package me.suwash.swagger.spec.manager.ws.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;

import com.fasterxml.jackson.annotation.JsonProperty;

@AllArgsConstructor
@Getter
public class ResponseBody {
    private Object payload;

    @JsonProperty("_errors")
    private CheckErrors errors;

    @JsonProperty("_warnings")
    private CheckErrors warnings;
}
