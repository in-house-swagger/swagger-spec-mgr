package me.suwash.swagger.spec.manager.infra.config;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.AbstractJackson2HttpMessageConverter;

final class YamlJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {
    YamlJackson2HttpMessageConverter() {
        super(new CustomYAMLMapper(), MediaType.parseMediaType("application/x-yaml"));
    }

}
