package me.suwash.swagger.spec.manager.ap;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.service.SpecService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpecFacade extends BaseFacade {

    @Autowired
    private SpecService service;

    public List<String> idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        return service.idList();
    }

    public Spec findById(final CommitInfo commitInfo, final String specId) {
        registerCommitInfo(commitInfo);

        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        return finded;
    }

    public Spec add(final CommitInfo commitInfo, final String specId, final Object payload) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("payload"));

        registerCommitInfo(commitInfo);

        final Spec spec = service.newSpec(specId, payload);
        spec.add();
        return service.findById(specId);
    }

    public Spec update(final CommitInfo commitInfo, final String specId, final Object payload) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("payload"));

        registerCommitInfo(commitInfo);

        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        finded.update(payload);
        return service.findById(specId);
    }

    public void delete(final CommitInfo commitInfo, final String specId) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));

        registerCommitInfo(commitInfo);

        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        finded.delete();
    }
}
