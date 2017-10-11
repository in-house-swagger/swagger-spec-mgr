package me.suwash.swagger.spec.manager.infra.error;

import java.util.Locale;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrDdSource;
import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;

@Getter
public class CheckError {
  private String propertyKey;
  private String messageId;
  private String message;

  /**
   * コンストラクタ。
   *
   * @param propertyKey チェック対象項目
   * @param messageId メッセージID
   * @param messageArgs メッセージ引数
   */
  public CheckError(final String propertyKey, final String messageId, final Object[] messageArgs) {
    this.propertyKey = SpecMgrDdSource.getInstance().getName(propertyKey);
    this.messageId = messageId;
    this.message = SpecMgrMessageSource.getInstance().getSimpleMessage(messageId, messageArgs);
  }
}
