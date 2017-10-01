package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.service.SpecService;

@Component
public class SpecFacade extends BaseFacade {

  @Autowired
  private SpecService service;

  /**
   * Specification IDの一覧を返します。
   *
   * @param commitInfo コミット情報
   * @return Specification ID一覧DTO
   */
  public IdListDto idList(final CommitInfo commitInfo) {
    registerCommitInfo(commitInfo);

    List<String> result = service.idList();
    return new IdListDto(context, result);
  }

  /**
   * Specificationを検索します。
   *
   * @param commitInfo コミット情報
   * @param specId Specification ID
   * @return 検索後のSpecification DTO
   */
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

  /**
   * Specification を追加します。
   *
   * @param commitInfo コミット情報
   * @param specId Specification ID
   * @param payload 定義内容
   * @return 追加後のSpecification DTO
   */
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

  /**
   * Specificationを更新します。
   *
   * @param commitInfo コミット情報
   * @param specId Specification ID
   * @param payload 定義内容
   * @return 更新後のSpecification DTO
   */
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

  /**
   * Specificationを削除します。
   *
   * @param commitInfo コミット情報
   * @param specId Specification ID
   * @return 削除後のSpecification DTO
   */
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
