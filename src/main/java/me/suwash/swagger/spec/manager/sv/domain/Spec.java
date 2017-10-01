package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.SpecGen;
import me.suwash.swagger.spec.manager.sv.specification.SpecSpec;

public class Spec extends SpecGen {

  @NotNull
  private final SpecMgrContext context;
  @NotNull
  private final SpecSpec specSpec;
  @NotNull
  private final GitRepoRepository gitRepoRepository;
  @NotNull
  private final SpecRepository specRepository;

  /**
   * コンストラクタ。
   *
   * @param context コンテキスト
   * @param specSpec Specification Spec
   * @param gitRepository Gitリポジトリ操作Repository
   * @param specRepository Specification操作Repository
   * @param id Specification ID
   * @param payload 定義内容
   */
  public Spec(final SpecMgrContext context, final SpecSpec specSpec,
      final GitRepoRepository gitRepository, final SpecRepository specRepository, final String id,
      final Object payload) {

    super(id, payload);
    this.context = context;
    this.specSpec = specSpec;
    this.gitRepoRepository = gitRepository;
    this.specRepository = specRepository;
  }

  /**
   * Specificationを追加します。
   */
  public void add() {
    specSpec.canAdd(this);

    // specを追加
    specRepository.add(this);
    // 追加を反映
    gitRepoRepository.push();
  }

  /**
   * Specificationを更新します。
   *
   * @param payload 定義内容
   */
  public void update(final Object payload) {
    this.payload = payload;
    specSpec.canUpdate(this);

    // 事前に更新を取得
    gitRepoRepository.pull();
    // specを更新
    specRepository.update(this);
    // 更新を反映
    gitRepoRepository.push();
  }

  /**
   * Specificationを削除します。
   */
  public void delete() {
    specSpec.canDelete(this);

    // 事前に更新を取得
    gitRepoRepository.pull();
    // specを削除
    specRepository.delete(this.id);
    // 削除を反映
    gitRepoRepository.push();
  }
}
