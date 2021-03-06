package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;
import me.suwash.swagger.spec.manager.sv.da.GitTagRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.TagGen;
import me.suwash.swagger.spec.manager.sv.specification.TagSpec;

public class Tag extends TagGen {

  @NotNull
  private final TagSpec tagSpec;
  @NotNull
  private final GitTagRepository repository;

  /**
   * コンストラクタ。
   *
   * @param tagSpec タグSpec
   * @param repository Gitタグ操作Repository
   * @param id タグ名
   */
  public Tag(final TagSpec tagSpec, final GitTagRepository repository, final String id) {

    super(id, null);
    this.tagSpec = tagSpec;
    this.repository = repository;
  }

  /**
   * タグを追加します。
   *
   * @param gitObject 作成元gitオブジェクト
   */
  public void add(final String gitObject) {
    this.gitObject = gitObject;
    tagSpec.canAdd(this);

    repository.addTag(this.gitObject, this.id);
  }

  /**
   * タグをリネームします。
   *
   * @param toTag リネーム先タグ名
   */
  public void rename(final String toTag) {
    tagSpec.canRename(this, toTag);

    repository.renameTag(this.id, toTag);
  }

  /**
   * タグを削除します。
   */
  public void delete() {
    tagSpec.canDelete(this);

    repository.removeTag(this.id);
  }
}
