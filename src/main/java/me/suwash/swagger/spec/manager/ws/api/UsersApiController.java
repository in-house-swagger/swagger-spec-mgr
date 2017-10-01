package me.suwash.swagger.spec.manager.ws.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import io.swagger.annotations.ApiParam;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.UserDto;
import me.suwash.swagger.spec.manager.ap.facade.UserFacade;
import me.suwash.swagger.spec.manager.ws.api.gen.UsersApi;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiController;
import me.suwash.swagger.spec.manager.ws.infra.BaseApiModelMapper.OperationType;
import me.suwash.swagger.spec.manager.ws.mapper.IdListApiModelMapper;
import me.suwash.swagger.spec.manager.ws.mapper.UsersApiModelMapper;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T09:16:20.502+09:00")
@Controller
public class UsersApiController extends BaseApiController implements UsersApi {

  @Autowired
  private UserFacade facade;

  @Override
  public ResponseEntity<Object> getUsers() {

    final IdListDto dto = facade.idList();

    final IdListApiModelMapper mapper = new IdListApiModelMapper(dto);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> addDefaultUser() {

    final UserDto dto = facade.addDefault();

    final UsersApiModelMapper mapper = new UsersApiModelMapper(dto, OperationType.CREATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> getUserById(@ApiParam(value = "user name for commit") @PathVariable(
      value = "userId", required = true) final String userId) {

    final UserDto dto = facade.findById(userId);

    final UsersApiModelMapper mapper = new UsersApiModelMapper(dto, OperationType.READ);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> addUserWithId(
      @ApiParam(value = "user name for commit") @PathVariable(value = "userId",
          required = true) final String userId,
      @ApiParam(value = "email address for commit") @RequestParam(value = "email",
          required = true) final String email) {

    final UserDto dto = facade.add(userId, email);

    final UsersApiModelMapper mapper = new UsersApiModelMapper(dto, OperationType.CREATE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

  @Override
  public ResponseEntity<Object> deleteUserById(
      @ApiParam(value = "user name for commit") @PathVariable(value = "userId",
          required = true) final String userId) {

    final UserDto dto = facade.delete(userId);

    final UsersApiModelMapper mapper = new UsersApiModelMapper(dto, OperationType.DELETE);
    return new ResponseEntity<Object>(mapper.getBody(), mapper.getHttpStatus());
  }

}
