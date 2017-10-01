package me.suwash.swagger.spec.manager.sv.domain.gen;

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
 * BranchGen。
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen",
    date = "2017-08-08T21:14:16.911+09:00")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class BranchGen {

  @JsonProperty("id")
  @NotEmpty(groups = {Read.class, Create.class, Update.class, Delete.class})
  protected String id = null;

  @JsonProperty("object")
  @NotEmpty(groups = {Create.class})
  protected String gitObject = null;
}
