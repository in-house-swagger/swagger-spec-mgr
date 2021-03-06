package me.suwash.swagger.spec.manager.da.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import me.suwash.swagger.spec.manager.da.infra.BaseRepository;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.specification.SpecSpec;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

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
    final List<String> idList = getSplitSpecIdList();
    return Collections.unmodifiableList(idList);
  }

  private List<String> getSplitSpecIdList() {
    final List<String> idList = new ArrayList<>();
    for (final File curDir : getSplitSpecDirList()) {
      idList.add(curDir.getName());
    }
    return idList;
  }

  private List<File> getSplitSpecDirList() {
    final String splitDir = specSpec.getSplitRootDir();
    if (!new File(splitDir).exists())
      return new ArrayList<>();

    return FindUtils.find(splitDir, 1, 1, FileType.Directory);
  }

  @Override
  public Spec findById(final String specId) {
    if (!idList().contains(specId))
      return null;

    // 返却用に統合ファイルを生成
    final String splitDir = specSpec.getSplitRootDir();
    final String mergedDir = specSpec.getMergedDir();
    SwaggerSpecUtils.writeMerged(splitDir, mergedDir, specId);

    // 統合ファイルのparse結果でSpecを返却
    final Object parsed = SwaggerSpecUtils.parse(mergedDir, specId);
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
    final String splitDir = specSpec.getSplitRootDir();
    final List<String> splitIgnoreRegexList = specSpec.getSplitIgnoreRegexList();
    SwaggerSpecUtils.writeSplit(payload, splitDir, specId, splitIgnoreRegexList);
  }

  @Override
  public void delete(final String specId) {
    SwaggerSpecUtils.deleteMerged(specSpec.getMergedDir(), specId);
    SwaggerSpecUtils.deleteSplit(specSpec.getSplitRootDir(), specId);
  }

}
