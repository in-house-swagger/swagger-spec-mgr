package me.suwash.swagger.spec.manager.ws.mapper;

import me.suwash.swagger.spec.manager.ap.dto.UserDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;
import me.suwash.swagger.spec.manager.ws.model.UsersApiModel;

import org.springframework.http.HttpStatus;

public class UsersApiModelMapper extends BaseApiModelMapper {
    public UsersApiModelMapper(final UserDto dto, final OperationType operation) {
        if (OperationType.read.equals(operation) && dto.getUser() == null) {
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
                this.body = newBody(new UsersApiModel(dto), dto);
                break;
            case read:
                this.httpStatus = HttpStatus.OK;
                this.body = newBody(new UsersApiModel(dto), dto);
                break;
            case delete:
            default:
                this.httpStatus = HttpStatus.OK;
                break;
        }
    }
}
