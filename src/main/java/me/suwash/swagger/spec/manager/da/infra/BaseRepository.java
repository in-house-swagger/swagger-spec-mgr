package me.suwash.swagger.spec.manager.da.infra;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseRepository {
    @Autowired
    protected ApplicationProperties props;
}
