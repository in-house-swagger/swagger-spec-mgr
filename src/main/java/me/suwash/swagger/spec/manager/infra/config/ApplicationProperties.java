package me.suwash.swagger.spec.manager.infra.config;

import java.io.IOException;
import java.util.Properties;

import me.suwash.swagger.spec.manager.infra.exception.SpecMgrException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class ApplicationProperties {
    private static final String PATH_PROPS = "/application.properties";

    private static ApplicationProperties instance;
    private static Properties props;

    private ApplicationProperties() {
        try {
            final String dirConfig = System.getProperty("dir.config");
            if (StringUtils.isEmpty(dirConfig)) {
                // classpathから読み込み
                props = PropertiesLoaderUtils.loadProperties(new ClassPathResource(PATH_PROPS));

            } else {
                // ファイルパスから読み込み
                final ResourceLoader loader = new FileSystemResourceLoader();
                final Resource resource = loader.getResource(dirConfig + PATH_PROPS);
                props = PropertiesLoaderUtils.loadProperties(new EncodedResource(resource, "utf8"));
            }
        } catch (IOException e) {
            throw new SpecMgrException("SpecMgr.03002", new Object[] {PATH_PROPS, e.getMessage()}, e);
        }
    }

    public static ApplicationProperties getInstance() {
        if (instance == null) {
            instance = new ApplicationProperties();
        }
        return instance;
    }

    public String getProperty(final String key) {
        return props.getProperty(key);
    }
}
