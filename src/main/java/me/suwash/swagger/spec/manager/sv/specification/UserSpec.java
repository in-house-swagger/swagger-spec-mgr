package me.suwash.swagger.spec.manager.sv.specification;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

@Component
public class UserSpec extends BaseSpec {

  @Autowired
  private ApplicationProperties props;

  /**
   * 検索の妥当性をチェックします。
   *
   * @param user コミットユーザ
   */
  public void canFind(final User user) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(user, Read.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    // 関連データチェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 追加の妥当性をチェックします。
   *
   * @param user コミットユーザ
   */
  public void canAdd(final User user) {
    boolean isValid = true;
    // 単項目チェック
    if (!isValid(user, Create.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 関連データチェック
    if (!mustNotExist(user))
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 削除の妥当性をチェックします。
   *
   * @param user コミットユーザ
   */
  public void canDelete(final User user) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(user, Delete.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 関連データチェック
    if (!mustExist(user))
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * ユーザルートディレクトリを返します。
   *
   * @return ユーザルートディレクトリ
   */
  public String getUsersDir() {
    return props.getDirData();
  }

  /**
   * ユーザディレクトリを返します。
   *
   * @param user コミットユーザ
   * @return ユーザディレクトリ
   */
  public String getUserDir(final User user) {
    return getUsersDir() + "/" + user.getId();
  }

  private boolean mustNotExist(final User user) {
    if (new File(getUserDir(user)).isDirectory()) {
      try {
        ValidationUtils.mustNotExistData("User", "User.id", user.getId(), this);
      } catch (SpecMgrException e) {
        addError(User.class, e);
        return false;
      }
    }
    return true;
  }

  private boolean mustExist(final User user) {
    if (!new File(getUserDir(user)).isDirectory()) {
      try {
        ValidationUtils.mustExistData("User", "User.id", user.getId(), null);
      } catch (SpecMgrException e) {
        addError(User.class, e);
        return false;
      }
    }
    return true;
  }
}
