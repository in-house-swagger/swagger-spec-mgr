package me.suwash.swagger.spec.manager.infra.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;

@Component
public class SpecMgrContext {
  private static final String KEY_ERRORS = "__ERRORS__";
  private static final String KEY_WARNINGS = "__WARNINGS__";

  private Map<String, Map<String, Object>> contexts = new HashMap<>();

  private Map<String, Object> getContext(final String contextKey) {
    ValidationUtils.mustNotEmpty("contextKey", contextKey);

    final boolean hasContext = contexts.containsKey(contextKey);
    if (hasContext)
      return contexts.get(contextKey);

    final Map<String, Object> context = new HashMap<>();
    contexts.put(contextKey, context);
    return context;

  }

  /**
   * コンテキストに値を登録します。
   *
   * @param contextKey コンテキストキー
   * @param key キー
   * @param value 設定値
   */
  public void put(final String contextKey, final String key, final Object value) {
    final Map<String, Object> context = getContext(contextKey);
    context.put(key, value);
  }

  /**
   * コンテキストから値を取得します。
   *
   * @param contextKey コンテキストキー
   * @param key キー
   * @return 設定値
   */
  public Object get(final String contextKey, final String key) {
    final Map<String, Object> context = getContext(contextKey);
    return context.get(key);
  }

  /**
   * コンテキストから値を削除します。
   *
   * @param contextKey コンテキストキー
   * @param key キー
   */
  public void remove(final String contextKey, final String key) {
    final Map<String, Object> context = getContext(contextKey);
    context.remove(key);
  }

  /**
   * コンテキストをクリアします。
   *
   * @param contextKey コンテキストキー
   */
  public void clear(final String contextKey) {
    final Map<String, Object> context = getContext(contextKey);
    context.clear();
  }

  /**
   * コンテキストのキーセットを返します。
   *
   * @param contextKey コンテキストキー
   * @return キーセット
   */
  public Set<String> keySet(final String contextKey) {
    final Map<String, Object> context = getContext(contextKey);
    return context.keySet();
  }

  /**
   * コンテキストにキーが存在するか確認します。
   *
   * @param contextKey コンテキストキー
   * @param key キー
   * @return 存在する場合、true
   */
  public boolean containsKey(final String contextKey, final String key) {
    final Map<String, Object> context = getContext(contextKey);
    return context.containsKey(key);
  }

  /**
   * 現在のthreadコンテキストにコミット情報を登録します。
   *
   * @param commitInfo コミット情報
   */
  public void putCommitInfo(final CommitInfo commitInfo) {
    put(getThreadContextKey(), CommitInfo.class.getName(), commitInfo);
  }

  /**
   * 現在のthreadコンテキストからコミット情報を取得します。
   *
   * @return コミット情報
   */
  public CommitInfo getCommitInfo() {
    return (CommitInfo) get(getThreadContextKey(), CommitInfo.class.getName());
  }

  /**
   * 現在のthreadコンテキストからエラー情報を取得します。
   *
   * @return エラー情報
   */
  public CheckErrors getErrors() {
    final Object errors = get(getThreadContextKey(), KEY_ERRORS);
    if (errors != null)
      return (CheckErrors) errors;
    return new CheckErrors();
  }

  /**
   * 現在のthreadコンテキストにエラー情報を一括追加します。
   *
   * @param violations 妥当性チェック結果
   */
  public <T> void addErrors(Set<ConstraintViolation<T>> violations) {
    final CheckErrors errors = getErrors();
    errors.addViolations(violations);
    putErrors(errors);
  }

  /**
   * 現在のthreadコンテキストにエラー情報を追加します。
   *
   * @param type 対象クラス
   * @param messageId エラーメッセージID
   * @param messageArgs メッセージ引数
   */
  public void addError(final Class<?> type, final String messageId, final Object... messageArgs) {
    final CheckErrors errors = getErrors();
    errors.add(type, messageId, messageArgs);
    putErrors(errors);
  }

  /**
   * 現在のthreadコンテキストからエラー情報をクリアします。
   */
  public void clearErrors() {
    final CheckErrors errors = getErrors();
    errors.clear();
    putErrors(errors);
  }

  private void putErrors(final CheckErrors errors) {
    put(getThreadContextKey(), KEY_ERRORS, errors);
  }

  /**
   * 現在のthreadコンテキストから警告情報を取得します。
   *
   * @return 警告情報
   */
  public CheckErrors getWarnings() {
    final Object errors = get(getThreadContextKey(), KEY_WARNINGS);
    if (errors != null)
      return (CheckErrors) errors;
    return new CheckErrors();
  }

  /**
   * 現在のthreadコンテキストに警告情報を一括追加します。
   *
   * @param violations 妥当性チェック結果
   */
  public <T> void addWarnings(Set<ConstraintViolation<T>> violations) {
    final CheckErrors errors = getWarnings();
    errors.addViolations(violations);
    putWarnings(errors);
  }

  /**
   * 現在のthreadコンテキストに警告情報を追加します。
   *
   * @param type 対象クラス
   * @param messageId 警告メッセージID
   * @param messageArgs メッセージ引数
   */
  public void addWarning(final Class<?> type, final String messageId, final Object... messageArgs) {
    final CheckErrors errors = getWarnings();
    errors.add(type, messageId, messageArgs);
    putWarnings(errors);
  }

  /**
   * 現在のthreadコンテキストから警告情報をクリアします。
   */
  public void clearWarnings() {
    final CheckErrors errors = getWarnings();
    errors.clear();
    putWarnings(errors);
  }

  private void putWarnings(final CheckErrors errors) {
    put(getThreadContextKey(), KEY_WARNINGS, errors);
  }

  /**
   * 現在のthreadコンテキストにアクセスするキーを返します。
   *
   * @return threadコンテキストキー
   */
  public String getThreadContextKey() {
    return Thread.currentThread().getName();
  }

}
