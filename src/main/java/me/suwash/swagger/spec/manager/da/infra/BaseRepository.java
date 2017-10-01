package me.suwash.swagger.spec.manager.da.infra;

import org.springframework.beans.factory.annotation.Autowired;
import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;

public abstract class BaseRepository {
  @Autowired
  protected ApplicationProperties props;
}
