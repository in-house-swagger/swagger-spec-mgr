package me.suwash.swagger.spec.manager.infra.error;

import java.util.Locale;

import me.suwash.swagger.spec.manager.infra.i18n.SpecMgrMessageSource;
import me.suwash.util.exception.LayerException;
import me.suwash.util.i18n.MessageSource;

/**
 * 基底例外クラス。
 */
public class SpecMgrException extends LayerException {

    private static final long serialVersionUID = 1L;

    public static final String MSGCD_ERRORHANDLE = "unexpectedError";

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     */
    public SpecMgrException(String messageId) {
        super(messageId);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param locale ロケール
     */
    public SpecMgrException(String messageId, Locale locale) {
        super(messageId, locale);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param messageArgs メッセージ引数
     */
    public SpecMgrException(String messageId, Object[] messageArgs) {
        super(messageId, messageArgs);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param messageArgs メッセージ引数
     * @param locale ロケール
     */
    public SpecMgrException(String messageId, Object[] messageArgs, Locale locale) {
        super(messageId, messageArgs, locale);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param cause ラップする例外オブジェクト
     */
    public SpecMgrException(String messageId, Throwable cause) {
        super(messageId, cause);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param locale ロケール
     * @param cause ラップする例外オブジェクト
     */
    public SpecMgrException(String messageId, Locale locale, Throwable cause) {
        super(messageId, locale, cause);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param messageArgs メッセージ引数
     * @param cause ラップする例外オブジェクト
     */
    public SpecMgrException(String messageId, Object[] messageArgs, Throwable cause) {
        super(messageId, messageArgs, cause);
    }

    /**
     * コンストラクタ。
     *
     * @param messageId メッセージID
     * @param messageArgs メッセージ引数
     * @param locale ロケール
     * @param cause ラップする例外オブジェクト
     */
    public SpecMgrException(String messageId, Object[] messageArgs, Locale locale, Throwable cause) {
        super(messageId, messageArgs, locale, cause);
    }

    /**
     * MessageSourceを返します。
     */
    @Override
    protected MessageSource getMessageSource() {
        return SpecMgrMessageSource.getInstance();
    }

    /*
     * (非 Javadoc)
     * @see me.suwash.util.exception.LayerException#getMessage()
     */
    @Override
    public String getMessage() {
        return messageSource.getMessage(messageId, messageArgs, locale);
    }

    public static Object[] array(final Object... args) {
        return args;
    }
}
