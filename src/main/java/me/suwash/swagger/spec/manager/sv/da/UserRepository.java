package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;
import me.suwash.swagger.spec.manager.sv.domain.User;

public interface UserRepository {

  /**
   * コミットユーザ名一覧を返します。
   * 
   * @return コミットユーザ名一覧
   */
  List<String> idList();

  /**
   * コミットユーザを検索します。
   *
   * @param userId コミットユーザ名
   * @return コミットユーザ
   */
  User findById(String userId);

  /**
   * コミットユーザを追加します。
   *
   * @param user コミットユーザ
   */
  void add(User user);

  // void update(User user);

  /**
   * コミットユーザを削除します。
   *
   * @param user コミットユーザ
   */
  void delete(User user);
}
