package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

public interface GitTagRepository {

  /**
   * タグ名一覧を返します。
   *
   * @return タグ名一覧
   */
  List<String> tagList();

  /**
   * タグの存在を確認します。
   *
   * @param name タグ名
   * @return 存在する場合、true
   */
  boolean isExistTag(String name);

  /**
   * タグを追加します。
   *
   * @param from 作成元gitオブジェクト
   * @param to タグ名
   */
  void addTag(String from, String to);

  /**
   * タグをリネームします。
   *
   * @param from リネーム元タグ名
   * @param to リネーム先タグ名
   */
  void renameTag(String from, String to);

  /**
   * タグを削除します。
   *
   * @param name タグ名
   */
  void removeTag(String name);

}
