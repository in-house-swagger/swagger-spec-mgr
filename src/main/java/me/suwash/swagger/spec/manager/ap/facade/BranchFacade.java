package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ap.dto.BranchListDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.service.BranchService;

@Component
public class BranchFacade extends BaseFacade {

  @Autowired
  private BranchService service;

  /**
   * ブランチ名の一覧を返します。
   *
   * @param commitInfo コミット情報
   * @return ブランチ名一覧DTO
   */
  public BranchListDto branchList(final CommitInfo commitInfo) {
    registerCommitInfo(commitInfo);

    List<String> list = service.idList();
    String current = service.current();
    return new BranchListDto(context, current, list);
  }

  /**
   * ブランチを検索します。
   *
   * @param commitInfo コミット情報
   * @param branchId ブランチ名
   * @return 検索後のブランチDTO
   */
  public BranchDto findById(final CommitInfo commitInfo, final String branchId) {
    registerCommitInfo(commitInfo);

    Branch result = null;
    try {
      result = service.findById(branchId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, result);
  }

  /**
   * ブランチを追加します。
   *
   * @param commitInfo コミット情報
   * @param gitObject 作成元gitオブジェクト
   * @param branchId ブランチ名
   * @return 追加後のブランチDTO
   */
  public BranchDto add(final CommitInfo commitInfo, final String gitObject, final String branchId) {
    registerCommitInfo(commitInfo);

    Branch result = null;
    try {
      result = service.addBranch(gitObject, branchId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, result);
  }

  /**
   * ブランチをリネームします。
   *
   * @param commitInfo コミット情報
   * @param fromBranch リネーム元ブランチ名
   * @param toBranch リネーム先ブランチ名
   * @return リネーム後のブランチDTO
   */
  public BranchDto rename(final CommitInfo commitInfo, final String fromBranch,
      final String toBranch) {
    registerCommitInfo(commitInfo);

    Branch result = null;
    try {
      result = service.renameBranch(fromBranch, toBranch);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, result);
  }

  /**
   * ブランチを削除します。
   *
   * @param commitInfo コミット情報
   * @param branchId ブランチ名
   * @return 削除後のブランチDTO
   */
  public BranchDto delete(final CommitInfo commitInfo, final String branchId) {
    registerCommitInfo(commitInfo);

    try {
      service.deleteBranch(branchId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, null);
  }

  /**
   * ブランチを切り替えます。
   *
   * @param commitInfo コミット情報
   * @param toBranch 切り替え先ブランチ名
   * @return 切り替え後のブランチDTO
   */
  public BranchDto switchBranch(final CommitInfo commitInfo, final String toBranch) {
    registerCommitInfo(commitInfo);

    Branch result = null;
    try {
      result = service.switchBranch(toBranch);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, result);
  }

  /**
   * ブランチをマージします。
   *
   * @param commitInfo コミット情報
   * @param fromBranch マージ元ブランチ名
   * @param toBranch マージ先ブランチ名
   * @return マージ後のブランチDTO
   */
  public BranchDto mergeBranch(final CommitInfo commitInfo, final String fromBranch,
      final String toBranch) {
    registerCommitInfo(commitInfo);

    Branch result = null;
    try {
      result = service.mergeBranch(fromBranch, toBranch);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new BranchDto(context, result);
  }

}
