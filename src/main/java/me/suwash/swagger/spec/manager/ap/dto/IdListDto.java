package me.suwash.swagger.spec.manager.ap.dto;

import java.util.List;
import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;

@Getter
public class IdListDto extends BaseDto {

  private List<String> list;

  public IdListDto(final SpecMgrContext context, final List<String> list) {
    super(context);
    this.list = list;
  }
}
