package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.specification.SpecSpec;

@Service
public class SpecService {

  @Autowired
  private SpecMgrContext context;
  @Autowired
  private SpecSpec specSpec;
  @Autowired
  private SpecRepository specRepository;
  @Autowired
  private GitRepoRepository gitRepoRepository;

  private Spec newSpec(final String specId, final Object payload) {
    return new Spec(context, specSpec, gitRepoRepository, specRepository, specId, payload);
  }

  /**
   * SpecificationID一覧を返します。
   *
   * @return SpecificationID一覧
   */
  public List<String> idList() {
    return specRepository.idList();
  }

  /**
   * Specificationを検索します。
   *
   * @param specId Specification ID
   * @return 検索後のSpecification
   */
  public Spec findById(final String specId) {
    final Spec criteria = newSpec(specId, null);
    specSpec.canFind(criteria);

    final Spec finded = specRepository.findById(specId);
    ValidationUtils.mustExistData(Spec.class.getSimpleName(), "id", specId, finded);
    return finded;
  }

  /**
   * Specificationを追加します。
   *
   * @param specId Specification ID
   * @param payload 定義内容
   * @return 追加後のSpecification
   */
  public Spec addSpec(final String specId, final Object payload) {
    final Spec spec = newSpec(specId, payload);
    spec.add();
    return findById(specId);
  }

  /**
   * Specificationを更新します。
   *
   * @param specId Specification ID
   * @param payload 定義内容
   * @return 更新後のSpecification
   */
  public Spec updateSpec(final String specId, final Object payload) {
    final Spec finded = newSpec(specId, null);
    finded.update(payload);
    return findById(specId);
  }

  /**
   * Specificationを削除します。
   *
   * @param specId Specification ID
   */
  public void deleteSpec(final String specId) {
    final Spec finded = newSpec(specId, null);
    finded.delete();
  }

}
