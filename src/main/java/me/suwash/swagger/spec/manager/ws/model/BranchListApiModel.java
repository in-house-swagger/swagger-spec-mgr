package me.suwash.swagger.spec.manager.ws.model;

import me.suwash.swagger.spec.manager.ap.dto.BranchListDto;
import me.suwash.swagger.spec.manager.ws.model.gen.BranchListApiModelGen;

public class BranchListApiModel extends BranchListApiModelGen {
  public BranchListApiModel(final BranchListDto dto) {
    super(dto.getCurrent(), dto.getList());
  }
}
