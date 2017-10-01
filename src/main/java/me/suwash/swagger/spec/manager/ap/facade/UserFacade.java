package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.UserDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.swagger.spec.manager.sv.service.UserService;

@Component
public class UserFacade extends BaseFacade {

  @Autowired
  private UserService service;

  /**
   * コミットユーザ名の一覧を返します。
   *
   * @return コミットユーザ名一覧DTO
   */
  public IdListDto idList() {
    List<String> result = service.idList();
    return new IdListDto(context, result);
  }

  /**
   * コミットユーザを検索します。
   *
   * @param userId コミットユーザ名
   * @return 検索後のコミットユーザDTO
   */
  public UserDto findById(final String userId) {
    User result = null;
    try {
      result = service.findById(userId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new UserDto(context, result);
  }

  /**
   * デフォルトのコミットユーザを追加します。
   *
   * @return 追加後のコミットユーザDTO
   */
  public UserDto addDefault() {
    User result = null;
    try {
      result = service.addDefaultUser();
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new UserDto(context, result);
  }

  /**
   * コミットユーザを追加します。
   *
   * @param userId ユーザ名
   * @param email email
   * @return 追加後のコミットユーザDTO
   */
  public UserDto add(final String userId, final String email) {
    User result = null;
    try {
      result = service.addUser(userId, email);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new UserDto(context, result);
  }

  /**
   * コミットユーザを削除します。
   *
   * @param userId ユーザ名
   * @return 削除後のコミットユーザDTO
   */
  public UserDto delete(final String userId) {
    try {
      service.deleteUser(userId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new UserDto(context, null);
  }
}
