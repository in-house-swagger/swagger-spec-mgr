package me.suwash.swagger.spec.manager.ap.dto;

import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.domain.Branch;

@Getter
public class BranchDto extends BaseDto {

    private Branch branch;

    public BranchDto(final SpecMgrContext context, final Branch branch) {
        super(context);
        this.branch = branch;
    }
}
