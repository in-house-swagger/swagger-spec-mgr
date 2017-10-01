package me.suwash.swagger.spec.manager.ap.dto;

import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.domain.Spec;

@Getter
public class SpecDto extends BaseDto {

  private Spec spec;

  public SpecDto(final SpecMgrContext context, final Spec spec) {
    super(context);
    this.spec = spec;
  }
}
