package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

import me.suwash.swagger.spec.manager.sv.domain.Spec;

public interface SpecRepository {

    List<String> idList();

    Spec findById(String specId);

    void add(Spec spec);

    void update(Spec spec);

    void delete(String specId);
}
