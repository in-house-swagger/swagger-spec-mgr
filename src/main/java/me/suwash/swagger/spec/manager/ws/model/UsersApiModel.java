package me.suwash.swagger.spec.manager.ws.model;

import me.suwash.swagger.spec.manager.ap.dto.UserDto;
import me.suwash.swagger.spec.manager.ws.model.gen.UsersApiModelGen;

public class UsersApiModel extends UsersApiModelGen {
    public UsersApiModel(final UserDto dto) {
        super(dto.getUser().getId());
    }
}
