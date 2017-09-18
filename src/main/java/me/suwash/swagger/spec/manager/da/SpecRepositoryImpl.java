package me.suwash.swagger.spec.manager.da;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SpecRepositoryImpl implements SpecRepository {

    @Autowired
    private ApplicationProperties props;
    @Autowired
    private SpecMgrContext context;
    @Autowired
    private GitRepoRepository gitRepository;

    private final String DIRNAME_MERGED = "res";

    @Override
    public List<String> idList() {
        final List<String> idList = getMergedSpecIdList();
        return Collections.unmodifiableList(idList);
    }

    private List<String> getMergedSpecIdList() {
        final List<String> idList = new ArrayList<>();
        for (final File curDir : getMergedSpecDirList()) {
            idList.add(curDir.getName());
        }
        return idList;
    }

    private List<File> getMergedSpecDirList() {
        final String mergedDir = getMergedDir();
        if (! new File(mergedDir).exists()) return new ArrayList<>();

        return FindUtils.find(getMergedDir(), 1, 1, FileType.Directory);
    }

    private String getMergedDir() {
        return props.getUserDir(currentCommitInfo()) + "/" + DIRNAME_MERGED;
    }

    private String getSplitDir() {
        return props.getDirSpecs(currentCommitInfo());
    }

    private CommitInfo currentCommitInfo() {
        final String threadName = Thread.currentThread().getName();
        return (CommitInfo) context.get(threadName, CommitInfo.class.getName());
    }

    @Override
    public Spec findById(final String specId) {
        final Object parsed = SwaggerSpecUtils.parse(getMergedDir(), specId);
        if (parsed == null) return null;

        return new Spec(gitRepository, this, specId, parsed);
    }

    @Override
    public void add(final Spec spec) {
        validate(spec);

        final File outputDir = new File(getSplitOutputDir(spec));
        if (outputDir.isDirectory())
            throw new SpecMgrException(
                MessageConst.DATA_ALREADY_EXIST, array("specs", "specId", spec.getId()));

        writeSpec(spec.getId(), spec.getPayload());
    }

    private String getSplitOutputDir(final Spec spec) {
        return SwaggerSpecUtils.getSplitOutputDir(getSplitDir(), spec.getId());
    }

    private void writeSpec(final String specId, final Object payload) {
        final List<String> splitIgnoreRegexList = props.getSplitIgnoreRegexList();
        SwaggerSpecUtils.writeSplit(payload, getSplitDir(), specId, splitIgnoreRegexList);
        SwaggerSpecUtils.writeMerged(getSplitDir(), getMergedDir(), specId);
    }

    @Override
    public void update(final Spec spec) {
        validate(spec);

        final File outputDir = new File(getSplitOutputDir(spec));
        if (! outputDir.isDirectory())
            throw new SpecMgrException(
                MessageConst.DATA_NOT_EXIST, array("specs", "specId", spec.getId()));

        writeSpec(spec.getId(), spec.getPayload());
    }

    @Override
    public void delete(final String specId) {
        if (StringUtils.isEmpty(specId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"Spec.id"});

        deleteSpec(specId);
    }

    private void deleteSpec(final String specId) {
        SwaggerSpecUtils.deleteMerged(getMergedDir(), specId);
        SwaggerSpecUtils.deleteSplit(getSplitDir(), specId);
    }

    // TODO BeanValidationに変更＆Specificationに集約して、実施も、domain持ちかな。
    private void validate(final Spec spec) {
        if (spec == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"Spec"});
        if (StringUtils.isEmpty(spec.getId()))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"Spec.id"});
        if (spec.getPayload() == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"Spec.payload"});
    }
}
