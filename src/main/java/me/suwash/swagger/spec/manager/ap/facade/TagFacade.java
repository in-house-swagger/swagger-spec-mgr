package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.TagDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.service.TagService;

@Component
public class TagFacade extends BaseFacade {

  @Autowired
  private TagService service;

  /**
   * タグ名の一覧を返します。
   *
   * @param commitInfo コミット情報
   * @return タグ名一覧DTO
   */
  public IdListDto idList(final CommitInfo commitInfo) {
    registerCommitInfo(commitInfo);

    List<String> result = service.idList();
    return new IdListDto(context, result);
  }

  /**
   * タグを検索します。
   *
   * @param commitInfo コミット情報
   * @param tagId タグ名
   * @return 検索後のタグDTO
   */
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

  /**
   * タグを追加します。
   *
   * @param commitInfo コミット情報
   * @param gitObject 作成元gitオブジェクト
   * @param tagId タグ名
   * @return 追加後のタグDTO
   */
  public TagDto add(final CommitInfo commitInfo, final String gitObject, final String tagId) {
    registerCommitInfo(commitInfo);

    Tag result = null;
    try {
      result = service.addTag(gitObject, tagId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new TagDto(context, result);
  }

  /**
   * タグをリネームします。
   *
   * @param commitInfo コミット情報
   * @param fromTag リネーム元タグ名
   * @param toTag リネーム先タグ名
   * @return リネーム後のタグDTO
   */
  public TagDto rename(final CommitInfo commitInfo, final String fromTag, final String toTag) {
    registerCommitInfo(commitInfo);

    Tag result = null;
    try {
      result = service.renameTag(fromTag, toTag);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new TagDto(context, result);
  }

  /**
   * タグを削除します。
   *
   * @param commitInfo コミット情報
   * @param tagId タグ名
   * @return 削除後のタグDTO
   */
  public TagDto delete(final CommitInfo commitInfo, final String tagId) {
    registerCommitInfo(commitInfo);

    try {
      service.deleteTag(tagId);
    } catch (SpecMgrException e) {
      handleApplicationException(e);
    }
    return new TagDto(context, null);
  }
}
