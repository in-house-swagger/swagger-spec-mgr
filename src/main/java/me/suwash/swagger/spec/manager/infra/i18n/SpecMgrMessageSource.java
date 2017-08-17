package me.suwash.swagger.spec.manager.infra.i18n;

import java.util.Locale;

import me.suwash.util.i18n.MessageSource;

/**
 * メッセージ用プロパティファイルの定義保持クラス。
 */
public class SpecMgrMessageSource extends MessageSource {
    private static SpecMgrMessageSource instance = new SpecMgrMessageSource();

    /**
     * Singletonパターンでオブジェクトを返します。
     *
     * @return Messageオブジェクト
     */
    public static SpecMgrMessageSource getInstance() {
        return instance;
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
     * @see me.suwash.util.i18n.MessageSource#getMessage(java.lang.String, java.lang.Object[], java.util.Locale)
     */
    @Override
    public String getMessage(String messageId, Object[] args, Locale locale) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(messageId).append("]")
            .append(super.getMessage(messageId, args, locale));
        return sb.toString();
    }
}
