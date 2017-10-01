package me.suwash.swagger.spec.manager.ws.mapper;

import org.springframework.http.HttpStatus;
import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;
import me.suwash.swagger.spec.manager.ws.model.BranchesApiModel;

public class BranchesApiModelMapper extends BaseApiModelMapper {

  /**
   * コンストラクタ。
   *
   * @param dto DTO
   * @param operation 操作
   */
  public BranchesApiModelMapper(final BranchDto dto, final OperationType operation) {
    if (OperationType.READ.equals(operation) && dto.getBranch() == null) {
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
      case CREATE:
        this.httpStatus = HttpStatus.CREATED;
        this.body = newBody(new BranchesApiModel(dto), dto);
        break;
      case READ:
      case UPDATE:
      case RENAME:
        this.httpStatus = HttpStatus.OK;
        this.body = newBody(new BranchesApiModel(dto), dto);
        break;
      case DELETE:
      default:
        this.httpStatus = HttpStatus.OK;
        break;
    }
  }
}
