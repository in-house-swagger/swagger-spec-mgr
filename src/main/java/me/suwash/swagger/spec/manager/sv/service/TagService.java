package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.sv.da.GitTagRepository;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.specification.TagSpec;

@Service
public class TagService {

  @Autowired
  private TagSpec tagSpec;
  @Autowired
  private GitTagRepository repository;

  private Tag newTag(final String tag) {
    return new Tag(tagSpec, repository, tag);
  }

  /**
   * タグID一覧を返します。
   *
   * @return タグID一覧
   */
  public List<String> idList() {
    return repository.tagList();
  }

  /**
   * タグを検索します。
   *
   * @param tagId タグ名
   * @return 検索後のタグ
   */
  public Tag findById(final String tagId) {
    final Tag criteria = newTag(tagId);
    tagSpec.canFind(criteria);

    Tag finded = null;
    if (repository.isExistTag(tagId))
      finded = newTag(tagId);
    ValidationUtils.existData(Tag.class.getSimpleName(), "id", tagId, finded);
    return finded;
  }

  /**
   * タグを追加します。
   *
   * @param gitObject 作成元gitオブジェクト
   * @param tagId タグ名
   * @return 追加後のタグ
   */
  public Tag addTag(final String gitObject, final String tagId) {
    final Tag tag = newTag(tagId);
    tag.add(gitObject);
    return findById(tagId);
  }

  /**
   * タグをリネームします。
   *
   * @param fromTag リネーム元タグ名
   * @param toTag リネーム先タグ名
   * @return リネーム後のタグ
   */
  public Tag renameTag(final String fromTag, final String toTag) {
    final Tag finded = newTag(fromTag);
    finded.rename(toTag);
    return findById(toTag);
  }

  /**
   * タグを削除します。
   *
   * @param tagId タグ名
   */
  public void deleteTag(final String tagId) {
    final Tag tag = newTag(tagId);
    tag.delete();
  }

}
