package me.suwash.swagger.spec.manager.ap.dto;

import java.util.List;
import lombok.Getter;
import me.suwash.swagger.spec.manager.ap.infra.BaseDto;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;

@Getter
public class BranchListDto extends BaseDto {

  private String current;
  private List<String> list;

  /**
   * コンストラクタ。
   *
   * @param context コンテキスト
   * @param current カレントブランチ名
   * @param list ブランチ名リスト
   */
  public BranchListDto(final SpecMgrContext context, final String current,
      final List<String> list) {
    super(context);
    this.current = current;
    this.list = list;
  }
}
