package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.service.SpecService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpecFacade extends BaseFacade {

    @Autowired
    private SpecService service;

    public IdListDto idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        List<String> result = service.idList();
        return new IdListDto(context, result);
    }

    public SpecDto findById(final CommitInfo commitInfo, final String specId) {
        registerCommitInfo(commitInfo);

        Spec result = null;
        try {
            result = service.findById(specId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new SpecDto(context, result);
    }

    public SpecDto add(final CommitInfo commitInfo, final String specId, final Object payload) {
        registerCommitInfo(commitInfo);

        Spec result = null;
        try {
            result = service.addSpec(specId, payload);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new SpecDto(context, result);
    }

    public SpecDto update(final CommitInfo commitInfo, final String specId, final Object payload) {
        registerCommitInfo(commitInfo);

        Spec result = null;
        try {
            result = service.updateSpec(specId, payload);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new SpecDto(context, result);
    }

    public SpecDto delete(final CommitInfo commitInfo, final String specId) {
        registerCommitInfo(commitInfo);

        try {
            service.deleteSpec(specId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new SpecDto(context, null);
    }
}
