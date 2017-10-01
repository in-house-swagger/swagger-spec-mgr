package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;
import me.suwash.swagger.spec.manager.sv.da.GitBranchRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.BranchGen;
import me.suwash.swagger.spec.manager.sv.specification.BranchSpec;

public class Branch extends BranchGen {

  @NotNull
  private final BranchSpec branchSpec;
  @NotNull
  private GitBranchRepository repository;

  /**
   * コンストラクタ。
   *
   * @param branchSpec ブランチSpecification
   * @param repository ブランチ操作repository
   * @param id ブランチID
   */
  public Branch(final BranchSpec branchSpec, final GitBranchRepository repository,
      final String id) {
    super(id, null);
    this.branchSpec = branchSpec;
    this.repository = repository;
  }

  /**
   * ブランチを追加します。
   *
   * @param gitObject 作成元gitオブジェクト
   */
  public void add(final String gitObject) {
    this.gitObject = gitObject;
    branchSpec.canAdd(this);

    repository.addBranch(this.gitObject, this.id);
  }

  /**
   * ブランチをリネームします。
   *
   * @param toBranch リネーム先ブランチ名
   */
  public void rename(final String toBranch) {
    branchSpec.canRename(this, toBranch);

    repository.renameBranch(this.id, toBranch);
  }

  /**
   * ブランチを削除します。
   */
  public void delete() {
    branchSpec.canDelete(this);

    repository.removeBranch(this.id);
  }

  /**
   * ブランチをマージします。
   *
   * @param to マージ先ブランチ
   */
  public void mergeInto(final Branch to) {
    branchSpec.canMerge(this, to);

    repository.mergeBranch(this.id, to.getId());
  }

  /**
   * ブランチを切り替えます。
   */
  public void switchBranch() {
    branchSpec.canSwitchBranch(this);

    repository.switchBranch(this.id);
  }
}
