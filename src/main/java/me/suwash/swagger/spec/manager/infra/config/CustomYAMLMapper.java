package me.suwash.swagger.spec.manager.infra.config;

import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public class CustomYAMLMapper extends YAMLMapper {
    private static final long serialVersionUID = 1L;

    public CustomYAMLMapper() {
        this.getFactory().configure(Feature.WRITE_DOC_START_MARKER, false);
    }
}
