package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.GitTagRepository;
import me.suwash.swagger.spec.manager.sv.domain.Tag;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagService {

    @Autowired
    private GitTagRepository repository;

    public List<String> idList() {
        return repository.tagList();
    }

    public Tag findById(final String tag) {
        if (StringUtils.isEmpty(tag))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"tag"});

        if (repository.isExistTag(tag)) return newTag(tag);
        return null;
    }

    public Tag newTag(final String tag) {
        if (StringUtils.isEmpty(tag))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"tag"});

        return new Tag(repository, tag);
    }
}
