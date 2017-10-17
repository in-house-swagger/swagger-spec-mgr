package me.suwash.swagger.spec.manager.ws.mapper;

import org.springframework.http.HttpStatus;
import me.suwash.swagger.spec.manager.ap.dto.BranchListDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;
import me.suwash.swagger.spec.manager.ws.model.BranchListApiModel;

public class BranchListApiModelMapper extends BaseApiModelMapper {

  /**
   * コンストラクタ。
   *
   * @param dto DTO
   */
  public BranchListApiModelMapper(final BranchListDto dto) {
    if (dto.hasError()) {
      this.httpStatus = HttpStatus.BAD_REQUEST;
      this.body = newBody(null, dto);
      return;
    }

    this.httpStatus = HttpStatus.OK;
    this.body = newBody(new BranchListApiModel(dto), dto);
  }
}
