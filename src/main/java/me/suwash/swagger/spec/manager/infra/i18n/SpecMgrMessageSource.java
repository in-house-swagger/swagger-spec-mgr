package me.suwash.swagger.spec.manager.infra.i18n;

import java.util.Locale;
import me.suwash.util.i18n.MessageSource;

/**
 * メッセージ用プロパティファイルの定義保持クラス。
 */
public class SpecMgrMessageSource extends MessageSource {
  private static SpecMgrMessageSource specMgrMessageSource = new SpecMgrMessageSource();

  /**
   * Singletonパターンでオブジェクトを返します。
   *
   * @return Messageオブジェクト
   */
  public static SpecMgrMessageSource getInstance() {
    return specMgrMessageSource;
  }

  @Override
  protected MessageSource getParent() {
    return MessageSource.getInstance();
  }

  @Override
  protected SpecMgrDdSource getDd() {
    return SpecMgrDdSource.getInstance();
  }

  /*
   * (非 Javadoc)
   *
   * @see me.suwash.util.i18n.MessageSource#getMessage(java.lang.String, java.lang.Object[],
   * java.util.Locale)
   */
  @Override
  public String getMessage(final String messageId, final Object[] args, final Locale locale) {
    StringBuilder sb = new StringBuilder();
    sb.append("[").append(messageId).append("] ").append(getSimpleMessage(messageId, args, locale));
    return sb.toString();
  }

  public String getSimpleMessage(final String messageId, final Object[] args) {
    return getSimpleMessage(messageId, args, Locale.getDefault());
  }

  public String getSimpleMessage(final String messageId, final Object[] args, Locale locale) {
    return super.getMessage(messageId, args, locale);
  }
}
