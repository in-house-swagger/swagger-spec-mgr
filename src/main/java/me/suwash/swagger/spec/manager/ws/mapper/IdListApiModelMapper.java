package me.suwash.swagger.spec.manager.ws.mapper;

import org.springframework.http.HttpStatus;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper;
import me.suwash.swagger.spec.manager.ws.model.IdListApiModel;

public class IdListApiModelMapper extends BaseApiModelMapper {

  /**
   * コンストラクタ。
   *
   * @param dto DTO
   */
  public IdListApiModelMapper(final IdListDto dto) {
    if (dto.hasError()) {
      this.httpStatus = HttpStatus.BAD_REQUEST;
      this.body = newBody(null, dto);
      return;
    }

    if (dto.getList().isEmpty()) {
      this.httpStatus = HttpStatus.NO_CONTENT;
      this.body = newBody(null, dto);
      return;
    }

    this.httpStatus = HttpStatus.OK;
    this.body = newBody(new IdListApiModel(dto), dto);
  }
}
