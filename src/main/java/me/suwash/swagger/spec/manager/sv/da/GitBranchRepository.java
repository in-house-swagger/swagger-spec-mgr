package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

public interface GitBranchRepository {

  /**
   * ブランチ名一覧を返します。
   *
   * @return ブランチ名一覧
   */
  List<String> branchList();

  /**
   * ブランチの存在を確認します。
   *
   * @param name ブランチ名
   * @return 存在する場合、true
   */
  boolean isExistBranch(String name);

  /**
   * ブランチを追加します。
   *
   * @param from 作成元gitオブジェクト
   * @param to ブランチ名
   */
  void addBranch(String from, String to);

  /**
   * ブランチをリネームします。
   *
   * @param from リネーム元ブランチ名
   * @param to リネーム先ブランチ名
   */
  void renameBranch(String from, String to);

  /**
   * ブランチを削除します。
   *
   * @param name ブランチ名
   */
  void removeBranch(String name);

  /**
   * ブランチをマージします。
   *
   * @param from マージ元ブランチ名
   * @param to マージ先ブランチ名
   */
  void mergeBranch(String from, String to);

  /**
   * ブランチを切り替えます。
   *
   * @param name 切り替え先ブランチ名
   */
  void switchBranch(String name);

}
