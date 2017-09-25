package me.suwash.swagger.spec.manager.ap;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.ScmInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.service.SpecService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpecFacade {

    @Autowired
    private SpecMgrContext context;

    @Autowired
    private SpecService service;

    private Object[] array(final Object... args) {
        return args;
    }

    private void registerScmInfo(final ScmInfo scmInfo) {
        final String threadName = Thread.currentThread().getName();
        this.context.put(threadName, ScmInfo.class.getName(), scmInfo);
    }

    public List<String> idList(final ScmInfo scmInfo) {
        registerScmInfo(scmInfo);
        return service.idList();
    }

    public Spec findById(final String specId, final ScmInfo scmInfo) {
        registerScmInfo(scmInfo);
        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        return finded;
    }

    public Spec add(final String specId, final Object payload, final ScmInfo scmInfo) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("payload"));

        registerScmInfo(scmInfo);
        final Spec spec = service.newSpec(specId, payload);
        spec.add();
        return service.findById(specId);
    }

    public Spec update(final String specId, final Object payload, final ScmInfo scmInfo) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("payload"));

        registerScmInfo(scmInfo);
        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        finded.update(payload);
        return service.findById(specId);
    }

    public void delete(final String specId, final ScmInfo scmInfo) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("specId"));

        registerScmInfo(scmInfo);
        final Spec finded = service.findById(specId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Spec", "specId", specId));

        finded.delete();
    }
}
