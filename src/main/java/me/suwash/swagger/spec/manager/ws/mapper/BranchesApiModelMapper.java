package me.suwash.swagger.spec.manager.ws.mapper;

import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;
import me.suwash.swagger.spec.manager.ws.model.BranchesApiModel;

import org.springframework.http.HttpStatus;

public class BranchesApiModelMapper extends BaseApiModelMapper {
    public BranchesApiModelMapper(final BranchDto dto, final OperationType operation) {
        if (OperationType.read.equals(operation) && dto.getBranch() == null) {
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
                this.body = newBody(new BranchesApiModel(dto), dto);
                break;
            case read:
            case update:
            case rename:
                this.httpStatus = HttpStatus.OK;
                this.body = newBody(new BranchesApiModel(dto), dto);
                break;
            case delete:
            default:
                this.httpStatus = HttpStatus.OK;
                break;
        }
    }
}