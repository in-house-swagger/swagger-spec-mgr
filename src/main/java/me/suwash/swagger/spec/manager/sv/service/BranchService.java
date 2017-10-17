package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.sv.da.GitBranchRepository;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.specification.BranchSpec;

@Service
public class BranchService {

  @Autowired
  private BranchSpec branchSpec;
  @Autowired
  private GitBranchRepository repository;

  private Branch newBranch(final String branch) {
    return new Branch(branchSpec, repository, branch);
  }

  /**
   * カレントブランチ名を返します。
   *
   * @return カレントブランチ名
   */
  public String current() {
    return repository.getCurrentBranch();
  }

  /**
   * ブランチ名の一覧を返します。
   *
   * @return ブランチ名一覧
   */
  public List<String> idList() {
    return repository.branchList();
  }

  /**
   * ブランチを検索します。
   *
   * @param branch ブランチ名
   * @return ブランチ
   */
  public Branch findById(final String branch) {
    final Branch criteria = newBranch(branch);
    branchSpec.canFind(criteria);

    Branch finded = null;
    if (repository.isExistBranch(branch))
      finded = newBranch(branch);
    ValidationUtils.mustExistData(Branch.class.getSimpleName(), "id", branch, finded);
    return finded;
  }

  /**
   * ブランチを追加します。
   *
   * @param gitObject 作成元gitオブジェクト
   * @param branchId ブランチ名
   * @return 作成したブランチ
   */
  public Branch addBranch(final String gitObject, final String branchId) {
    final Branch branch = newBranch(branchId);
    branch.add(gitObject);
    return findById(branchId);
  }

  /**
   * ブランチをリネームします。
   *
   * @param fromBranch リネーム元ブランチ名
   * @param toBranch リネーム先ブランチ名
   * @return リネーム後のブランチ
   */
  public Branch renameBranch(final String fromBranch, final String toBranch) {
    final Branch finded = newBranch(fromBranch);
    finded.rename(toBranch);
    return findById(toBranch);
  }

  /**
   * ブランチを削除します。
   *
   * @param branchId ブランチ名
   */
  public void deleteBranch(final String branchId) {
    final Branch finded = newBranch(branchId);
    finded.delete();
  }

  /**
   * ブランチを切り替えます。
   *
   * @param branch 切り替え先ブランチ名
   * @return 切り替え後のブランチ
   */
  public Branch switchBranch(final String branch) {
    final Branch toBranch = newBranch(branch);

    toBranch.switchBranch();
    return toBranch;
  }

  /**
   * ブランチをマージします。
   *
   * @param from マージ元ブランチ名
   * @param to マージ先ブランチ名
   * @return マージ後のブランチ
   */
  public Branch mergeBranch(final String from, final String to) {
    final Branch fromBranch = newBranch(from);
    final Branch toBranch = newBranch(to);

    fromBranch.mergeInto(toBranch);
    return toBranch;
  }

}
