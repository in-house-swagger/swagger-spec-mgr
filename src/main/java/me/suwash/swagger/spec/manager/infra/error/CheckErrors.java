package me.suwash.swagger.spec.manager.infra.error;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CheckErrors implements Iterable<CheckError> {

    @JsonProperty("list")
    private List<CheckError> list = new ArrayList<>();

    @Override
    public Iterator<CheckError> iterator() {
        return list.iterator();
    }

    public void clear() {
        list.clear();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int size() {
        return list.size();
    }

    public CheckError get(final int index) {
        return list.get(index);
    }

    public void add(final Class<?> type, final String messageId, final Object... messageArgs) {
        final CheckError error = new CheckError(type.getSimpleName(), messageId, messageArgs);
        list.add(error);
    }

    public <T> void addViolations(Set<ConstraintViolation<T>> violations) {
        violations.forEach(violation -> {
            list.add(convCheckError(violation));
        });
    }

    private <T> CheckError convCheckError(ConstraintViolation<T> violation) {
        final String typeName = violation.getRootBean().getClass().getSimpleName();
        return new CheckError(
            typeName + "." + violation.getPropertyPath().toString(),
            violation.getMessage(),
            violation.getExecutableParameters());
    }
}
