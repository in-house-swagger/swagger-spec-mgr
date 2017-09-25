package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.sv.da.UserRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.UserGen;
import me.suwash.swagger.spec.manager.sv.specification.UserSpec;

public class User extends UserGen {

    @NotNull
    private final UserSpec userSpec;
    @NotNull
    private final UserRepository userRepository;

    public User(
        final UserSpec userSpec,
        final UserRepository userRepository,
        final String id,
        final String email) {

        super(id, email);
        this.userSpec = userSpec;
        this.userRepository = userRepository;
    }

    public void add() {
        userSpec.canAdd(this);
        userRepository.add(this);
    }

    // public void update() {
    // userSpec.canUpdate(this);
    // userRepository.update(this);
    // }

    public void delete() {
        userSpec.canDelete(this);
        userRepository.delete(this);
    }
}
