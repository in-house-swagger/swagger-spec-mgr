package me.suwash.swagger.spec.manager.infra.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SpecMgrContext {
    private Map<String, Map<String, Object>> _contexts = new HashMap<>();

    private Object[] array(final Object... args) {
        return args;
    }

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
}
