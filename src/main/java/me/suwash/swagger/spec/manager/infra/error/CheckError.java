package me.suwash.swagger.spec.manager.infra.error;

import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrDdSource;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;

@Getter
public class CheckError {
  private String propertyKey;
  private String message;

  public CheckError(final String propertyKey, final String messageId, final Object[] messageArgs) {
    this.propertyKey = SpecMgrDdSource.getInstance().getName(propertyKey);
    this.message = SpecMgrMessageSource.getInstance().getMessage(messageId, messageArgs);
  }
}
