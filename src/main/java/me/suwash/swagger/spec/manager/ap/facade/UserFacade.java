package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.UserDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.swagger.spec.manager.sv.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserFacade extends BaseFacade {

    @Autowired
    private UserService service;

    public IdListDto idList() {
        List<String> result = service.idList();
        return new IdListDto(context, result);
    }

    public UserDto findById(final String userId) {
        User result = null;
        try {
            result = service.findById(userId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new UserDto(context, result);
    }

    public UserDto addDefault() {
        User result = null;
        try {
            result = service.addDefaultUser();
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new UserDto(context, result);
    }

    public UserDto add(final String userId, final String email) {
        User result = null;
        try {
            result = service.addUser(userId, email);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new UserDto(context, result);
    }

    public UserDto delete(final String userId) {
        try {
            service.deleteUser(userId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new UserDto(context, null);
    }
}
