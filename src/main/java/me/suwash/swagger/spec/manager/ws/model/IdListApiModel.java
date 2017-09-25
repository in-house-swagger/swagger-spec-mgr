package me.suwash.swagger.spec.manager.ws.model;

import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ws.model.gen.IdListApiModelGen;

public class IdListApiModel extends IdListApiModelGen {
    public IdListApiModel(final IdListDto dto) {
        super(dto.getList());
    }
}
