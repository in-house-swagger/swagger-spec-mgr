package me.suwash.swagger.spec.manager.sv.domain.gen;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TagGen
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2017-08-08T21:14:16.911+09:00")
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TagGen {

    @JsonProperty("id")
    @NotEmpty
    protected String id = null;

    @JsonProperty("object")
    @NotEmpty
    protected String gitObject = null;
}
