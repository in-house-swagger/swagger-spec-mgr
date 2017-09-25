package me.suwash.swagger.spec.manager.ap.dto;

import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.domain.Tag;

@Getter
public class TagDto extends BaseDto {

    private Tag tag;

    public TagDto(final SpecMgrContext context, final Tag tag) {
        super(context);
        this.tag = tag;
    }
}
