package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.GitTagRepository;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.specification.TagSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    @Autowired
    private TagSpec tagSpec;
    @Autowired
    private GitTagRepository repository;

    public Tag newTag(final String tag) {
        return new Tag(tagSpec, repository, tag);
    }

    public List<String> idList() {
        return repository.tagList();
    }

    public Tag findById(final String tag) {
        final Tag criteria = newTag(tag);
        tagSpec.canFind(criteria);

        if (repository.isExistTag(tag)) return newTag(tag);
        throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array(Tag.class.getSimpleName(), "id", tag));
    }

}
