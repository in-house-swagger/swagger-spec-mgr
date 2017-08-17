package me.suwash.swagger.spec.manager.ap;

import me.suwash.swagger.spec.manager.model.gen.Spec;
import me.suwash.swagger.spec.manager.sv.domain.SpecEntity;

import org.springframework.stereotype.Component;

@Component
public class SpecFacade {

    public Spec findById(final String specId) {
        final SpecEntity criteria = new SpecEntity();
        criteria.setId(specId);
        final SpecEntity result = criteria.findById();
        if (result == null) {
            return null;
        }

        return new Spec().id(result.getId()).payload(result.getPayload());
    }

    public Spec add(final String specId, final Object payload) {
        final SpecEntity entity = new SpecEntity();
        entity.setId(specId);
        entity.setPayload(payload);
        final SpecEntity result = entity.add();

        return new Spec().id(result.getId()).payload(result.getPayload());
    }

    public void delete(String specId) {
        final SpecEntity criteria = new SpecEntity();
        criteria.setId(specId);
        criteria.delete();
    }
}
