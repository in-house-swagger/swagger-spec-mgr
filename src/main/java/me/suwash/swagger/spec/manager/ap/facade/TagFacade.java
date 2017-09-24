package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.TagDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.service.TagService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TagFacade extends BaseFacade {

    @Autowired
    private TagService service;

    public IdListDto idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        List<String> result = service.idList();
        return new IdListDto(context, result);
    }

    public TagDto findById(final CommitInfo commitInfo, final String tagId) {
        registerCommitInfo(commitInfo);

        Tag result = null;
        try {
            result = service.findById(tagId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new TagDto(context, result);
    }

    public TagDto add(final CommitInfo commitInfo, final String gitObject, final String tagId) {
        registerCommitInfo(commitInfo);

        Tag result = null;
        try {
            final Tag tag = service.newTag(tagId);
            tag.add(gitObject);
            result = service.findById(tagId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new TagDto(context, result);
    }

    public TagDto rename(final CommitInfo commitInfo, final String fromTag, final String toTag) {
        registerCommitInfo(commitInfo);

        Tag result = null;
        try {
            final Tag finded = service.findById(fromTag);
            finded.rename(toTag);
            result = service.findById(toTag);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new TagDto(context, result);
    }

    public TagDto delete(final CommitInfo commitInfo, final String tagId) {
        registerCommitInfo(commitInfo);

        try {
            final Tag tag = service.newTag(tagId);
            tag.delete();
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new TagDto(context, null);
    }
}
