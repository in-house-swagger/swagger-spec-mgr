package me.suwash.swagger.spec.manager.sv.specification;

import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

@Component
public class TagSpec extends BaseSpec {

  /**
   * 検索の妥当性をチェックします。
   *
   * @param tag タグ
   */
  public void canFind(final Tag tag) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(tag, Read.class))
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
   * @param tag タグ
   */
  public void canAdd(final Tag tag) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(tag, Create.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    // 関連データチェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * リネームの妥当性をチェックします。
   *
   * @param tag タグ
   * @param toTag リネーム先タグ名
   */
  public void canRename(final Tag tag, final String toTag) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(tag, Update.class))
      isValid = false;

    // 複数項目関連チェック
    try {
      ValidationUtils.mustNotEmpty("toTag", toTag);
    } catch (SpecMgrException e) {
      addError(Tag.class, e);
      isValid = false;
    }

    // 関連データチェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 削除の妥当性をチェックします。
   *
   * @param tag タグ
   */
  public void canDelete(final Tag tag) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(tag, Delete.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    // 関連データチェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * タグが存在しないエラーを発生させます。
   *
   * @param id タグ名
   */
  public void notExist(final String id) {
    ValidationUtils.mustExistData("Tag", "Tag.id", id, null);
  }

  /**
   * タグがすでに存在するエラーを発生させます。
   *
   * @param id タグ名
   */
  public void alreadyExist(final String id) {
    ValidationUtils.mustNotExistData("Tag", "Tag.id", id, this);
  }
}
