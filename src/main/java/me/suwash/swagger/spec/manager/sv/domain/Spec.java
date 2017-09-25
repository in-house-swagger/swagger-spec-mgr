package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.SpecRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.SpecGen;

public class Spec extends SpecGen {

    @NotNull
    private final SpecRepository repository;

    public Spec(final SpecRepository repository, final String id, final Object payload) {
        super(id, payload);
        this.repository = repository;
    }

    public void add() {
        repository.add(this);
    }

    public void update(final Object payload) {
        if (payload == null)
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"payload"});
        this.payload = payload;
        repository.update(this);
    }

    public void delete() {
        repository.delete(this.id);
    }
}
