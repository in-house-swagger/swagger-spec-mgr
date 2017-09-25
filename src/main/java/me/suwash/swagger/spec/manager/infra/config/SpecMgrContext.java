package me.suwash.swagger.spec.manager.infra.config;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SpecMgrContext {
    private static final String KEY_ERRORS = "__ERRORS__";
    private static final String KEY_WARNINGS = "__WARNINGS__";

    private Map<String, Map<String, Object>> _contexts = new HashMap<>();

    private Map<String, Object> getContext(final String contextKey) {
        if (StringUtils.isEmpty(contextKey))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("contextKey"));

        final boolean hasContext = _contexts.containsKey(contextKey);
        if (hasContext) return _contexts.get(contextKey);

        final Map<String, Object> context = new HashMap<>();
        _contexts.put(contextKey, context);
        return context;

    }

    public void put(final String contextKey, final String key, final Object value) {
        final Map<String, Object> context = getContext(contextKey);
        context.put(key, value);
    }

    public Object get(final String contextKey, final String key) {
        final Map<String, Object> context = getContext(contextKey);
        return context.get(key);
    }

    public void remove(final String contextKey, final String key) {
        final Map<String, Object> context = getContext(contextKey);
        context.remove(key);
    }

    public void clear(final String contextKey) {
        final Map<String, Object> context = getContext(contextKey);
        context.clear();
    }

    public Set<String> keySet(final String contextKey) {
        final Map<String, Object> context = getContext(contextKey);
        return context.keySet();
    }

    public boolean containsKey(final String contextKey, final String key) {
        final Map<String, Object> context = getContext(contextKey);
        return context.containsKey(key);
    }

    public void putCommitInfo(final CommitInfo commitInfo) {
        put(getThreadContextKey(), CommitInfo.class.getName(), commitInfo);
    }

    public CommitInfo getCommitInfo() {
        return (CommitInfo) get(getThreadContextKey(), CommitInfo.class.getName());
    }

    public CheckErrors getErrors() {
        final Object errors = get(getThreadContextKey(), KEY_ERRORS);
        if (errors != null) return (CheckErrors) errors;
        return new CheckErrors();
    }

    public <T> void addErrors(Set<ConstraintViolation<T>> violations) {
        final CheckErrors errors = getErrors();
        errors.addViolations(violations);
        putErrors(errors);
    }

    public void addError(final Class<?> type, final String messageId, final Object... messageArgs) {
        final CheckErrors errors = getErrors();
        errors.add(type, messageId, messageArgs);
        putErrors(errors);
    }

    public void clearErrors() {
        final CheckErrors errors = getErrors();
        errors.clear();
        putErrors(errors);
    }

    private void putErrors(final CheckErrors errors) {
        put(getThreadContextKey(), KEY_ERRORS, errors);
    }

    public CheckErrors getWarnings() {
        final Object errors = get(getThreadContextKey(), KEY_WARNINGS);
        if (errors != null) return (CheckErrors) errors;
        return new CheckErrors();
    }

    public <T> void addWarnings(Set<ConstraintViolation<T>> violations) {
        final CheckErrors errors = getWarnings();
        errors.addViolations(violations);
        putWarnings(errors);
    }

    public void addWarning(final Class<?> type, final String messageId, final Object... messageArgs) {
        final CheckErrors errors = getWarnings();
        errors.add(type, messageId, messageArgs);
        putWarnings(errors);
    }

    public void clearWarnings() {
        final CheckErrors errors = getWarnings();
        errors.clear();
        putWarnings(errors);
    }

    private void putWarnings(final CheckErrors errors) {
        put(getThreadContextKey(), KEY_WARNINGS, errors);
    }

    public String getThreadContextKey() {
        return Thread.currentThread().getName();
    }

}
