package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.specification.SpecSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpecService {

    @Autowired
    private SpecSpec specSpec;
    @Autowired
    private SpecRepository specRepository;
    @Autowired
    private GitRepoRepository gitRepoRepository;

    public Spec newSpec(final String specId, final Object payload) {
        return new Spec(specSpec, gitRepoRepository, specRepository, specId, payload);
    }

    public List<String> idList() {
        return specRepository.idList();
    }

    public Spec findById(final String specId) {
        final Spec criteria = newSpec(specId, null);
        specSpec.canFind(criteria);

        final Spec finded = specRepository.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array(Spec.class.getSimpleName(), "id", specId));
        return finded;
    }

}
