package me.suwash.swagger.spec.manager.ws.infra;

import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;

import org.springframework.http.HttpStatus;

@Getter
public abstract class BaseApiModelMapper {

    public enum OperationType {
        CREATE, READ, UPDATE, DELETE, RENAME
    }

    protected HttpStatus httpStatus;
    protected Object body;

    protected ResponseBody newBody(final Object payload, final BaseDto dto) {
        return new ResponseBody(payload, dto.getErrors(), dto.getWarnings());
    }
}
