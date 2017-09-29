package me.suwash.swagger.spec.manager.sv.infra;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseSpec {

    public static final String SPECIFICATION_ERROR = "specificationError";

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

    protected void addError(final Class<?> type, final SpecMgrException e) {
        context.addError(type, e.getMessageId(), e.getMessageArgs());
    }
}
