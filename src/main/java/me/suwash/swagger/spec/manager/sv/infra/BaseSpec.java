package me.suwash.swagger.spec.manager.sv.infra;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseSpec {

    @Autowired
    private SpecMgrContext context;

    @Autowired
    private Validator validator;

    protected <T> boolean isValid(T target, Class<?>... classes) {
        Set<ConstraintViolation<T>> violations = validator.validate(target, classes);
        if (violations.isEmpty()) return true;

        context.addErrors(violations);
        return false;
    }

    protected void addError(final Class<?> type, final String messageId, final Object... messageArgs) {
        context.addError(type, messageId, messageArgs);
    }
}
