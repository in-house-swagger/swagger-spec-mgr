package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpecService {

    @Autowired
    private SpecRepository specRepository;
    @Autowired
    private GitRepoRepository gitRepoRepository;

    public List<String> idList() {
        return specRepository.idList();
    }

    public Spec findById(final String specId) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"specId"});

        return specRepository.findById(specId);
    }

    public Spec newSpec(final String specId, final Object payload) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"specId"});
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"payload"});

        return new Spec(gitRepoRepository, specRepository, specId, payload);
    }
}
