package me.suwash.swagger.spec.manager.sv.specification;

import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

@Component
public class BranchSpec extends BaseSpec {

  /**
   * 検索の妥当性をチェックします。
   *
   * @param branch ブランチ
   */
  public void canFind(final Branch branch) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(branch, Read.class))
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
   * @param branch ブランチ
   */
  public void canAdd(final Branch branch) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(branch, Create.class))
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
   * @param branch ブランチ
   * @param toBranch リネーム先ブランチ名
   */
  public void canRename(final Branch branch, final String toBranch) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(branch, Update.class))
      isValid = false;

    // 複数項目関連チェック
    try {
      ValidationUtils.mustNotEmpty("toBranch", toBranch);
    } catch (SpecMgrException e) {
      addError(Branch.class, e);
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
   * @param branch ブランチ
   */
  public void canDelete(final Branch branch) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(branch, Delete.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    // 関連データチェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * マージの妥当性をチェックします。
   *
   * @param from マージ元ブランチ
   * @param to マージ先ブランチ
   */
  public void canMerge(final Branch from, final Branch to) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(from, Read.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    // 関連データチェック
    try {
      ValidationUtils.mustNotEmpty("toBranch", to.getId());
    } catch (SpecMgrException e) {
      addError(Branch.class, e);
      isValid = false;
    }

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * ブランチ切り替えの妥当性をチェックします。
   *
   * @param branch ブランチ
   */
  public void canSwitchBranch(final Branch branch) {
    canFind(branch);
  }

  /**
   * ブランチが存在しないエラーを発生させます。
   *
   * @param id ブランチ名
   */
  public void notExist(final String id) {
    ValidationUtils.mustExistData("Branch", "Branch.id", id, null);
  }

  /**
   * ブランチがすでに存在するエラーを発生させます。
   *
   * @param id ブランチ名
   */
  public void alreadyExist(final String id) {
    ValidationUtils.mustNotExistData("Branch", "Branch.id", id, this);
  }
}
