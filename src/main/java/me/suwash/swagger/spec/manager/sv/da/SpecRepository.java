package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;
import me.suwash.swagger.spec.manager.sv.domain.Spec;

public interface SpecRepository {

  /**
   * Specification ID一覧を返します。
   *
   * @return Specification ID一覧
   */
  List<String> idList();

  /**
   * Specificationを検索します。
   *
   * @param specId Specification ID
   * @return Specification
   */
  Spec findById(String specId);

  /**
   * Specificationを追加します。
   *
   * @param spec Specification
   */
  void add(Spec spec);

  /**
   * Specificationを更新します。
   *
   * @param spec Specification
   */
  void update(Spec spec);

  /**
   * Specificationを削除します。
   *
   * @param specId Specification ID
   */
  void delete(String specId);
}
