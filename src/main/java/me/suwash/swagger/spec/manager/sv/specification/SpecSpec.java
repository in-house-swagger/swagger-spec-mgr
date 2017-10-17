package me.suwash.swagger.spec.manager.sv.specification;

import java.io.File;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

@Component
public class SpecSpec extends BaseSpec {

  private static final String DIRNAME_MERGED = "res";

  @Autowired
  private ApplicationProperties props;
  @Autowired
  private SpecMgrContext context;

  /**
   * 検索の妥当性をチェックします。
   *
   * @param spec Specification
   */
  public void canFind(final Spec spec) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(spec, Read.class))
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
   * @param spec Specification
   */
  public void canAdd(final Spec spec) {
    boolean isValid = true;
    // 単項目チェック
    if (!isValid(spec, Create.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 関連データチェック
    // ユーザディレクトリ
    if (!mustExistUserDir())
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 出力ディレクトリ
    if (!mustNotExist(spec))
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 更新の妥当性をチェックします。
   *
   * @param spec Specification
   */
  public void canUpdate(final Spec spec) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(spec, Update.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 関連データチェック
    isValid = mustExist(spec);

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 削除の妥当性をチェックします。
   *
   * @param spec Specification
   */
  public void canDelete(final Spec spec) {
    boolean isValid = true;

    // 単項目チェック
    if (!isValid(spec, Delete.class))
      isValid = false;

    // 複数項目関連チェック
    // なし

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);

    // 関連データチェック
    isValid = mustExist(spec);

    if (!isValid)
      throw new SpecMgrException(SPECIFICATION_ERROR);
  }

  /**
   * 分割ファイル出力ディレクトリを返します。
   *
   * @param spec Specification
   * @return 分割ファイル出力ディレクトリ
   */
  public String getSplitOutputDir(final Spec spec) {
    return SwaggerSpecUtils.getSplitDir(getSplitRootDir(), spec.getId());
  }

  /**
   * 分割ファイル出力ルートディレクトリを返します。
   *
   * @return 分割ファイル出漁ルートディレクトリ
   */
  public String getSplitRootDir() {
    return props.getDirSpecs(currentCommitInfo());
  }

  /**
   * 統合ファイル出力ディレクトリを返します。
   *
   * @return 統合ファイル出力ディレクトリ
   */
  public String getMergedDir() {
    return props.getUserDir(currentCommitInfo()) + "/" + DIRNAME_MERGED;
  }

  private CommitInfo currentCommitInfo() {
    return context.getCommitInfo();
  }

  /**
   * 分割除外パスの正規表現リストを返します。
   *
   * @return 分割除外パスの正規表現リスト
   */
  public List<String> getSplitIgnoreRegexList() {
    return props.getSplitIgnoreRegexList();
  }

  private boolean mustExistUserDir() {
    try {
      ValidationUtils.mustExistDir(props.getUserRepoDir(context.getCommitInfo()));
    } catch (SpecMgrException e) {
      addError(Spec.class, e);
      return false;
    }
    return true;
  }

  private boolean mustExist(final Spec spec) {
    if (!new File(getSplitOutputDir(spec)).isDirectory()) {
      try {
        ValidationUtils.mustExistData("Spec", "Spec.id", spec.getId(), null);
      } catch (SpecMgrException e) {
        addError(Spec.class, e);
        return false;
      }
    }
    return true;
  }

  private boolean mustNotExist(final Spec spec) {
    if (new File(getSplitOutputDir(spec)).isDirectory()) {
      try {
        ValidationUtils.mustNotExistData("Spec", "Spec.id", spec.getId(), spec);
      } catch (SpecMgrException e) {
        addError(Spec.class, e);
        return false;
      }
    }
    return true;
  }

}
