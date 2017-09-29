package me.suwash.swagger.spec.manager.ap.infra;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

import org.springframework.beans.factory.annotation.Autowired;

@lombok.extern.slf4j.Slf4j
public abstract class BaseFacade {

    @Autowired
    protected SpecMgrContext context;

    protected void registerCommitInfo(final CommitInfo commitInfo) {
        context.putCommitInfo(commitInfo);
    }

    protected void handleApplicationException(final SpecMgrException e) {
        log.error(e.getMessage(), e);
        if (!BaseSpec.SPECIFICATION_ERROR.equals(e.getMessageId()))
            context.addError(Spec.class, e.getMessageId(), e.getMessageArgs());
    }

}
