package me.suwash.swagger.spec.manager.sv.domain.gen;

import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;

/**
 * SpecGen。
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T21:14:16.911+09:00")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SpecGen {

  @JsonProperty("id")
  @NotEmpty(groups = {Read.class, Create.class, Update.class, Delete.class})
  protected String id = null;

  @JsonProperty("payload")
  @NotNull(groups = {Create.class, Update.class})
  protected Object payload = null;
}
