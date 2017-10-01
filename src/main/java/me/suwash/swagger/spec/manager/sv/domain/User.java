package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;
import me.suwash.swagger.spec.manager.sv.da.UserRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.UserGen;
import me.suwash.swagger.spec.manager.sv.specification.UserSpec;

public class User extends UserGen {

  @NotNull
  private final UserSpec userSpec;
  @NotNull
  private final UserRepository userRepository;

  /**
   * コンストラクタ。
   *
   * @param userSpec コミットユーザSpec
   * @param userRepository コミットユーザ操作Repository
   * @param id コミットユーザID
   * @param email コミットユーザemail
   */
  public User(final UserSpec userSpec, final UserRepository userRepository, final String id,
      final String email) {

    super(id, email);
    this.userSpec = userSpec;
    this.userRepository = userRepository;
  }

  /**
   * コミットユーザを追加します。
   */
  public void add() {
    userSpec.canAdd(this);
    userRepository.add(this);
  }

  // public void update() {
  // userSpec.canUpdate(this);
  // userRepository.update(this);
  // }

  /**
   * コミットユーザを削除します。
   */
  public void delete() {
    userSpec.canDelete(this);
    userRepository.delete(this);
  }
}
