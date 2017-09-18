package me.suwash.swagger.spec.manager.ap;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;

import org.springframework.beans.factory.annotation.Autowired;

public class BaseFacade {

    @Autowired
    protected SpecMgrContext context;

    protected void registerCommitInfo(final CommitInfo commitInfo) {
        final String threadName = Thread.currentThread().getName();
        this.context.put(threadName, CommitInfo.class.getName(), commitInfo);
    }

}
