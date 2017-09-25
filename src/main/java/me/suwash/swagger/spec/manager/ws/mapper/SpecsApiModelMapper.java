package me.suwash.swagger.spec.manager.ws.mapper;

import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;

import org.springframework.http.HttpStatus;

public class SpecsApiModelMapper extends BaseApiModelMapper {
    public SpecsApiModelMapper(final SpecDto dto, final OperationType operation) {
        if (OperationType.read.equals(operation) && dto.getSpec() == null) {
            this.httpStatus = HttpStatus.NOT_FOUND;
            this.body = newBody(null, dto);
            return;
        }

        if (dto.hasError()) {
            this.httpStatus = HttpStatus.BAD_REQUEST;
            this.body = newBody(null, dto);
            return;
        }

        switch (operation) {
            case create:
                this.httpStatus = HttpStatus.CREATED;
                this.body = dto.getSpec().getPayload();
                break;
            case read:
            case update:
                this.httpStatus = HttpStatus.OK;
                this.body = dto.getSpec().getPayload();
                break;
            case delete:
            default:
                this.httpStatus = HttpStatus.OK;
                this.body = null;
                break;
        }
    }
}
