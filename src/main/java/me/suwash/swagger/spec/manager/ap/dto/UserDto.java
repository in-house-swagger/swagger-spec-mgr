package me.suwash.swagger.spec.manager.ap.dto;

import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.domain.User;

@Getter
public class UserDto extends BaseDto {

  private User user;

  public UserDto(final SpecMgrContext context, final User user) {
    super(context);
    this.user = user;
  }
}
