package me.suwash.swagger.spec.manager.da;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.ScmInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SpecRepositoryImpl implements SpecRepository {

    private final ApplicationProperties props;
    private final SpecMgrContext context;
    private String defaultUser;
    private String dirData;
    private String relDirSpecs;
    private List<String> splitIgnoreRegexList;

    private final String DIRNAME_MERGED = "res";

    @Autowired
    public SpecRepositoryImpl(
        final ApplicationProperties props,
        final SpecMgrContext context) {
        this.props = props;
        this.context = context;
    }

    @PostConstruct
    public void loadProps() {
        this.defaultUser = props.getDefaultScmUser();
        this.dirData = props.getDirData();
        this.relDirSpecs = props.getRelDirSpecs();
        this.splitIgnoreRegexList = props.getSplitIgnoreRegexList();
    }

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
        return getBaseDir() + "/" + DIRNAME_MERGED;
    }

    private String getSplitDir() {
        return getBaseDir() + "/repo/" + this.relDirSpecs;
    }

    private String getBaseDir() {
        final String threadName = Thread.currentThread().getName();
        final ScmInfo scmInfo = (ScmInfo) context.get(threadName, ScmInfo.class.getName());

        if (scmInfo == null || StringUtils.isEmpty(scmInfo.getUser()))
            return this.dirData + "/" + this.defaultUser;

        return this.dirData + "/" + scmInfo.getUser();
    }

    @Override
    public Spec findById(@NotEmpty final String specId) {
        final Object parsed = SwaggerSpecUtils.parse(getMergedDir(), specId);
        if (parsed == null) return null;

        return new Spec(this, specId, parsed);
    }

    @Override
    public void add(@NotNull @Valid final Spec spec) {
        writeSpec(spec.getId(), spec.getPayload());
    }

    private void writeSpec(final String specId, final Object payload) {
        SwaggerSpecUtils.writeSplit(payload, getSplitDir(), specId, splitIgnoreRegexList);
        SwaggerSpecUtils.writeMerged(getSplitDir(), getMergedDir(), specId);
    }

    @Override
    public void update(@Valid final Spec spec) {
        add(spec);
    }

    @Override
    public void delete(@NotEmpty final String specId) {
        deleteSpec(specId);
    }

    private void deleteSpec(final String specId) {
        SwaggerSpecUtils.deleteMerged(getMergedDir(), specId);
        SwaggerSpecUtils.deleteSplit(getSplitDir(), specId);
    }

}
