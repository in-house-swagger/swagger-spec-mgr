package me.suwash.swagger.spec.manager.sv.domain;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.exception.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.model.gen.Spec;

public class SpecEntity extends Spec {

    private final String dirMerged;
    private final String dirSplit;
    private final List<String> splitIgnoreRegexList;

    public SpecEntity() {
        ApplicationProperties props = ApplicationProperties.getInstance();
        this.dirMerged = props.getProperty("me.suwash.swagger.spec.manager.dir.base.merged");
        this.dirSplit = props.getProperty("me.suwash.swagger.spec.manager.dir.base.split");
        this.splitIgnoreRegexList = Arrays.asList(props.getProperty("me.suwash.swagger.spec.manager.split.ignore.regex.list").split(","));
    }

    public SpecEntity findById() {
        if (StringUtils.isEmpty(this.getId())) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {"SpecEntity", "id"});
        }

        final String specId = this.getId();
        final Object parsed = SwaggerSpecUtils.parse(dirMerged, specId);
        if (parsed == null) {
            return null;
        }

        final SpecEntity finded = new SpecEntity();
        finded.setId(specId);
        finded.setPayload(parsed);
        return finded;
    }

    public SpecEntity add() {
        if (StringUtils.isEmpty(this.getId())) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {"SpecEntity", "id"});
        }
        if (this.getPayload() == null) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {"SpecEntity", "payload"});
        }

        final String specId = this.getId();
        SwaggerSpecUtils.writeSplit(this.getPayload(), dirSplit, specId, splitIgnoreRegexList);
        SwaggerSpecUtils.writeMerged(dirSplit, dirMerged, specId);

        SpecEntity added = new SpecEntity();
        added.setId(specId);
        added.setPayload(SwaggerSpecUtils.parse(dirMerged, specId));
        return added;
    }

    public SpecEntity update() {
        return add();
    }

    public void delete() {
        if (StringUtils.isEmpty(this.getId())) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {"SpecEntity", "id"});
        }

        final String specId = this.getId();
        SwaggerSpecUtils.deleteMerged(dirMerged, specId);
        SwaggerSpecUtils.deleteSplit(dirSplit, specId);
    }
}
