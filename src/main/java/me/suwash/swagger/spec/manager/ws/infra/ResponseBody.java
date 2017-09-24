package me.suwash.swagger.spec.manager.ws.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;

@AllArgsConstructor
@Getter
public class ResponseBody {
    private Object payload;
    private CheckErrors _errors;
    private CheckErrors _warnings;
}
