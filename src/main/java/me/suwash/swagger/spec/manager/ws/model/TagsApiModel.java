package me.suwash.swagger.spec.manager.ws.model;

import me.suwash.swagger.spec.manager.ap.dto.TagDto;
import me.suwash.swagger.spec.manager.ws.model.gen.TagsApiModelGen;

public class TagsApiModel extends TagsApiModelGen {
    public TagsApiModel(final TagDto dto) {
        super(dto.getTag().getId());
    }
}
