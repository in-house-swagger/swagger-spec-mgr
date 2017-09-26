package me.suwash.swagger.spec.manager.da.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.suwash.swagger.spec.manager.da.infra.BaseRepository;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.specification.SpecSpec;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SpecRepositoryImpl extends BaseRepository implements SpecRepository {

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecSpec specSpec;
    @Autowired
    private GitRepoRepository gitRepository;

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
        final String mergedDir = specSpec.getMergedDir();
        if (!new File(mergedDir).exists()) return new ArrayList<>();

        return FindUtils.find(mergedDir, 1, 1, FileType.Directory);
    }

    @Override
    public Spec findById(final String specId) {
        final Object parsed = SwaggerSpecUtils.parse(specSpec.getMergedDir(), specId);
        if (parsed == null) return null;

        return new Spec(context, specSpec, gitRepository, this, specId, parsed);
    }

    @Override
    public void add(final Spec spec) {
        writeSpec(spec.getId(), spec.getPayload());
    }

    @Override
    public void update(final Spec spec) {
        writeSpec(spec.getId(), spec.getPayload());
    }

    private void writeSpec(final String specId, final Object payload) {
        final String splitDir = specSpec.getSplitDir();
        final List<String> splitIgnoreRegexList = specSpec.getSplitIgnoreRegexList();
        SwaggerSpecUtils.writeSplit(payload, splitDir, specId, splitIgnoreRegexList);

        final String mergedDir = specSpec.getMergedDir();
        SwaggerSpecUtils.writeMerged(splitDir, mergedDir, specId);
    }

    @Override
    public void delete(final String specId) {
        SwaggerSpecUtils.deleteMerged(specSpec.getMergedDir(), specId);
        SwaggerSpecUtils.deleteSplit(specSpec.getSplitDir(), specId);
    }

}
