package me.suwash.swagger.spec.manager.ap.infra;

import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;

@Getter
public abstract class BaseDto {

  protected CheckErrors errors = new CheckErrors();
  protected CheckErrors warnings = new CheckErrors();

  protected BaseDto(final SpecMgrContext context) {
    // チェック結果をDTOに移動
    this.errors = context.getErrors();
    this.warnings = context.getWarnings();
    context.clear(context.getThreadContextKey());
  }

  public boolean hasError() {
    return !this.errors.isEmpty();
  }

  public boolean hasWarning() {
    return !this.warnings.isEmpty();
  }
}
