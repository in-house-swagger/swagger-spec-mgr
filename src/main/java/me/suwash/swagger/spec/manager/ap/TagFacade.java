package me.suwash.swagger.spec.manager.ap;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.service.TagService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagFacade extends BaseFacade {

    @Autowired
    private TagService service;

    public List<String> idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        return service.idList();
    }

    public Tag findById(final CommitInfo commitInfo, final String tagId) {
        registerCommitInfo(commitInfo);

        final Tag finded = service.findById(tagId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Tag", "tagId", tagId));

        return finded;
    }

    public Tag add(final CommitInfo commitInfo, final String gitObject, final String tagId) {
        if (StringUtils.isEmpty(gitObject))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("gitObject"));
        if (StringUtils.isEmpty(tagId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("tagId"));

        registerCommitInfo(commitInfo);

        final Tag tag = service.newTag(tagId);
        tag.add(gitObject);
        return service.findById(tagId);
    }

    public Tag rename(final CommitInfo commitInfo, final String fromTag, final String toTag) {
        if (StringUtils.isEmpty(fromTag))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("fromTag"));
        if (StringUtils.isEmpty(toTag))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("toTag"));

        registerCommitInfo(commitInfo);

        final Tag finded = service.findById(fromTag);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Tag", "tagId", fromTag));

        finded.rename(toTag);
        return service.findById(toTag);
    }

    public void delete(final CommitInfo commitInfo, final String tagId) {
        if (StringUtils.isEmpty(tagId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("tagId"));

        registerCommitInfo(commitInfo);

        final Tag finded = service.findById(tagId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Tag", "tagId", tagId));

        finded.delete();
    }
}
