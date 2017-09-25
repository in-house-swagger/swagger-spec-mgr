package me.suwash.swagger.spec.manager.ws.model;

import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ws.model.gen.BranchesApiModelGen;

public class BranchesApiModel extends BranchesApiModelGen {
    public BranchesApiModel(final BranchDto dto) {
        super(dto.getBranch().getId());
    }
}
